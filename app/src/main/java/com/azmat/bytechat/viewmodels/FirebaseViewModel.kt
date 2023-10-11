package com.azmat.bytechat.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azmat.bytechat.models.Chat
import com.azmat.bytechat.models.Message
import com.azmat.bytechat.models.User
import com.azmat.bytechat.models.UserChat
import com.azmat.bytechat.repositories.LoginRepository
import com.azmat.bytechat.utils.ResultState
import com.azmat.bytechat.utils.chatListPath
import com.azmat.bytechat.utils.messagePath
import com.azmat.bytechat.utils.usersPath
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseViewModel: ViewModel() {
    private val authRepository = LoginRepository()
    val db = FirebaseDatabase.getInstance()
    val auth = Firebase.auth
    private val storageRef = FirebaseStorage.getInstance()

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>>
        get() = _userList

    private val _chatList = MutableLiveData<List<UserChat>>()
    val chatList: LiveData<List<UserChat>>
        get() = _chatList

    private val _message = MutableLiveData<Message>()
    val message: LiveData<Message>
        get() = _message

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>>
        get() = _messages

    private val _result = MutableLiveData<ResultState<String>>()
    val result: LiveData<ResultState<String>>
        get() = _result

    private var seenListener: ValueEventListener? = null

    fun signInWithPhoneNumber(phoneNumber: String, activity: FragmentActivity, callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        authRepository.signInWithPhoneNumber(phoneNumber, activity, callback)
    }

    fun logout(){
        authRepository.logout()
        FirebaseMessaging.getInstance().deleteToken()
    }

    fun updateUser(user: User){
        val data = HashMap<String, Any>()
        data["id"] = user.id
        data["name"] = user.name
        data["phone"] = user.phone
        if(user.imgProfile!="") data["imgProfile"] = user.imgProfile
        db.getReference(usersPath).child(user.id).updateChildren(data)
    }

    fun updateProfilePhoto(uri: Any){
        val simpleFormatter = SimpleDateFormat("ddmmyyyyHHmmss", Locale.getDefault())
        val fileName = simpleFormatter.format(Date().time)
        val fileRef = storageRef.getReference("profile/$fileName")
        _result.value = ResultState.Loading

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (_result.value == ResultState.Loading) {
                _result.value = ResultState.Error("timed out while uploading your profile picture")
            }
        }
        handler.postDelayed(timeoutRunnable, 10000)

        try {
            val uploadTask = when (uri) {
                is Uri -> {
                    fileRef.putFile(uri)
                }
                is Bitmap -> {
                    val baos = ByteArrayOutputStream()
                    uri.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    fileRef.putBytes(data)
                }
                else -> throw IllegalArgumentException("Unsupported image type")
            }

            uploadTask.addOnSuccessListener {
                handler.removeCallbacks(timeoutRunnable)
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()
                    val data = HashMap<String, Any>()
                    data["imgProfile"] = url
                    db.getReference(usersPath).child(auth.currentUser?.uid!!).updateChildren(data)
                    _result.value = ResultState.Success("Image uploaded successfully")
                }
            }.addOnFailureListener { exception ->
                handler.removeCallbacks(timeoutRunnable)
                _result.value = ResultState.Error(exception.message ?: "Unknown error")
            }.addOnCanceledListener {
                handler.removeCallbacks(timeoutRunnable)
                _result.value = ResultState.Error("Canceled")
            }
        } catch (e: Exception) {
            handler.removeCallbacks(timeoutRunnable)
            _result.value = ResultState.Error(e.message ?: "Unknown error")
        }
    }

    fun fetchUsers(){
        db.getReference(usersPath).orderByChild("name").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val users = mutableListOf<User>()
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        if(userSnapshot.key==auth.currentUser?.uid) continue
                        user?.id = userSnapshot.key!!
                        user?.let {
                            users.add(it)
                        }
                    }
                    _userList.value = users
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun sendMessage(message: Message){
        message.messageId = db.getReference(messagePath).push().key!!
        message.time = System.currentTimeMillis()
        db.getReference(messagePath).child(message.messageId).setValue(message)
            .addOnSuccessListener {
                db.getReference(chatListPath).child(message.senderId)
                    .child(message.receiverId).setValue(mapOf(
                        "id" to message.receiverId,
                        "lastMessage" to message.message,
                        "timestamp" to message.time,
                        "senderId" to message.senderId
                    ))
                db.getReference(chatListPath).child(message.receiverId)
                    .child(message.senderId).setValue(mapOf(
                        "id" to message.senderId,
                        "lastMessage" to message.message,
                        "timestamp" to message.time,
                        "senderId" to message.senderId
                    ))
            }
    }

    fun fetchMessage(receiverId: String){
        val messageList = mutableListOf<Message>()
        db.getReference(messagePath).orderByChild("time").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if(message!=null){
                    if(
                        (message.senderId ==auth.currentUser?.uid && message.receiverId ==receiverId) || (message.senderId ==receiverId && message.receiverId==auth.currentUser?.uid)
                    ){
                        messageList.add(message)
                        _messages.value = messageList
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun seenMessage(userId: String){
        seenListener = db.getReference(messagePath).addValueEventListener(object :
            ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(messageSnapshot in snapshot.children){
                    val message = messageSnapshot.getValue(Message::class.java)
                    if((message!!.receiverId == auth.currentUser?.uid) && (message.senderId == userId)){
                        val data = HashMap<String, Any>()
                        data["seen"] = true
                        messageSnapshot.ref.updateChildren(data)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun fetchChats() {
        val userId = auth.currentUser?.uid
        val chatsList = mutableListOf<UserChat>()

        db.getReference(chatListPath).child(userId!!)
            .orderByChild("timestamp").addChildEventListener(object :
            ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chat = snapshot.getValue(Chat::class.java)
                    val chatId = snapshot.key

                    if (chat != null && chatId != null) {
                        db.getReference(usersPath).child(chat.id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    if (userSnapshot.exists()) {
                                        val user = userSnapshot.getValue(User::class.java)
                                        val userChat = UserChat(user!!, chat)
                                        chatsList.add(userChat)
                                        chatsList.sortByDescending { it.chat.timestamp }
                                        _chatList.value = chatsList
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val updatedChat = snapshot.getValue(Chat::class.java)
                    val chatId = snapshot.key

                    if (updatedChat != null && chatId != null) {
                        val index = chatsList.indexOfFirst { it.chat.id == chatId }
                        if (index != -1) {
                            chatsList[index] = UserChat(chatsList[index].user, updatedChat)
                            chatsList.sortByDescending { it.chat.timestamp }
                            _chatList.value = chatsList
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }


    fun stopSeenMassageListener(){
        db.getReference(messagePath).removeEventListener(seenListener!!)
    }
}