package com.azmat.bytechat.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.azmat.bytechat.BuildConfig
import com.azmat.bytechat.databinding.FragmentMessageBinding
import com.azmat.bytechat.models.Message
import com.azmat.bytechat.models.User
import com.azmat.bytechat.ui.adapters.MessageRecyclerViewAdapter
import com.azmat.bytechat.utils.usersPath
import com.azmat.bytechat.viewmodels.FirebaseViewModel
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var mViewModel: FirebaseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        val adapter = MessageRecyclerViewAdapter()

        var receiverId: String? = null
        var user: User? = null
        arguments?.let {
            val args = MessageFragmentArgs.fromBundle(it)
            user = args.user
            receiverId = user?.id
        }

        binding.title.text = user?.name
        if(user?.imgProfile!=""){
            Glide.with(requireContext())
                .load(user?.imgProfile)
                .centerCrop()
                .dontAnimate()
                .into(binding.profileImg)
        }

        val recycler = binding.recyclerView
        recycler.adapter = adapter

        mViewModel.fetchMessage(receiverId!!)


        mViewModel.messages.observe(viewLifecycleOwner){
            if(it!=null){
                adapter.setData(it)
            }
        }

        mViewModel.seenMessage(receiverId!!)

        mViewModel.message.observe(viewLifecycleOwner){
            if(it!=null){
                if((it.senderId==mViewModel.auth.currentUser?.uid && it.receiverId==receiverId)
                    || (it.senderId==receiverId && it.receiverId==mViewModel.auth.currentUser?.uid)){
                    adapter.addMessage(it)
                }
            }
        }

        binding.btnSend.setOnClickListener {
            val message = Message(
                mViewModel.auth.currentUser?.uid!!,
                receiverId!!,
                binding.message.text.toString()
            )
            mViewModel.sendMessage(message)
            sendNotification(user!!, message)
            binding.message.setText("")

            recycler.smoothScrollToPosition(adapter.itemCount)
        }

        binding.navigateUp.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun sendNotification(user: User, sentMessage: Message){
        mViewModel.db.getReference(usersPath).child(sentMessage.senderId).addListenerForSingleValueEvent(object :
        ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val sender = snapshot.getValue(User::class.java)
                    val title = sender!!.name
                    val recipientToken = user.fcmToken
                    val message = sentMessage.message

                    val notification = JSONObject()
                    val notificationData = JSONObject()

                    notificationData.put("title", title)
                    notificationData.put("message", message)
                    notificationData.put("senderId", sentMessage.senderId)
                    notification.put("to", recipientToken)
                    notification.put("data", notificationData)

                    val fcmUrl = "https://fcm.googleapis.com/fcm/send"
                    val apiKey = BuildConfig.API_TOKEN

                    val request = Request.Builder()
                        .url(fcmUrl)
                        .post(RequestBody.create(MediaType.parse("application/json"), notification.toString()))
                        .addHeader("Authorization", "key=$apiKey")
                        .addHeader("Content-Type", "application/json")
                        .build()

                    val client = OkHttpClient()
                    client.newCall(request).enqueue(object :
                        Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("FCM", "Notification sending failed")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.d("FCM", "Notification sent successfully")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    override fun onPause() {
        super.onPause()
        mViewModel.stopSeenMassageListener()
    }
}