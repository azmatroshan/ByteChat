package com.azmat.bytechat.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.azmat.bytechat.R
import com.azmat.bytechat.databinding.FragmentChatBinding
import com.azmat.bytechat.ui.adapters.ChatRecyclerViewAdapter
import com.azmat.bytechat.utils.usersPath
import com.azmat.bytechat.viewmodels.FirebaseViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigationView: NavigationView
    private lateinit var mViewModel: FirebaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.onPrimary)
        mViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]

        generateToken()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawerLayout = binding.drawerLayout
        toolbar = binding.toolbar
        navigationView = binding.navigationView

        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        navigationView.setNavigationItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.drawer_log_out -> { logoutAlert() }
                R.id.update_profile -> { findNavController().navigate(ChatFragmentDirections.actionChatFragmentToProfileFragment()) }
                R.id.share_app -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,
                            getString(R.string.share_app_text))
                    }
                    context?.startActivity(shareIntent)
                }
                R.id.source_code -> {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.repo_link) )
                    )
                    context?.startActivity(urlIntent)
                }
            }
            binding.drawerLayout.closeDrawer(navigationView)
            true
        }

        toolbar.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId){
                R.id.log_out -> {
                    logoutAlert()
                    true
                }
                R.id.search_bar -> {

                    true
                }
                else -> false
            }
        }

        binding.fabNewChat.setOnClickListener {
            findNavController().navigate(ChatFragmentDirections.actionChatFragmentToContactFragment())
        }

        val adapter = ChatRecyclerViewAdapter()
        adapter.onItemClick = {
            findNavController().navigate(ChatFragmentDirections.actionChatFragmentToMessageFragment(it))
        }
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mViewModel.fetchChats()
        mViewModel.chatList.observe(viewLifecycleOwner){
            if(it!=null){
                adapter.setData(it)
            }
        }
    }

    private fun logout(){
        mViewModel.logout()
        findNavController().navigate(ChatFragmentDirections.actionChatFragmentToLoginFragment())
    }

    private fun logoutAlert(){
        val alertDialogLogout = AlertDialog.Builder(requireContext())
        alertDialogLogout.setTitle(getString(R.string.log_out))
        alertDialogLogout.setMessage(getString(R.string.logout_confirmation))
        alertDialogLogout.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogLogout.setPositiveButton(getString(R.string.yes)){ dialog, _ ->
            logout()
            dialog.dismiss()
        }
        alertDialogLogout.show()
    }

    private fun generateToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {task->
            if(task.isSuccessful){
                mViewModel.db.getReference(usersPath).child(mViewModel.auth.currentUser?.uid!!).updateChildren(mapOf("fcmToken" to task.result))
            }
        }
    }
}