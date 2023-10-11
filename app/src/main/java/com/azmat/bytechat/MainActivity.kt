package com.azmat.bytechat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.azmat.bytechat.databinding.ActivityMainBinding
import com.azmat.bytechat.models.User
import com.azmat.bytechat.ui.fragments.ChatFragmentDirections
import com.azmat.bytechat.utils.usersPath
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (intent != null && intent.action == "Open messageFragment") {
            val userId = intent.getStringExtra("userId")
            Log.d("UserId", userId!!)
            FirebaseDatabase.getInstance().getReference(usersPath).child(userId)
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                findNavController(R.id.fragmentContainerView)
                                    .navigate(ChatFragmentDirections.actionChatFragmentToMessageFragment(user))
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}