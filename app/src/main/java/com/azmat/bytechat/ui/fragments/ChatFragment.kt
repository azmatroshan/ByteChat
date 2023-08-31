package com.azmat.bytechat.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.azmat.bytechat.viewmodels.LoginViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigationView: NavigationView
    private lateinit var mViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.onPrimary)
        mViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

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
                R.id.drawer_log_out -> { logout() }
            }
            binding.drawerLayout.closeDrawer(navigationView)
            true
        }

        toolbar.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId){
                R.id.log_out -> {
                    logout()
                    true
                }
                R.id.search_bar -> {
                    featureNotImplemented()
                    true
                }
                else -> false
            }
        }

        val adapter = ChatRecyclerViewAdapter()
        adapter.onItemClick = {
            featureNotImplemented()
        }
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun logout(){
        mViewModel.logout()
        findNavController().navigate(ChatFragmentDirections.actionChatFragmentToLoginFragment())
    }

    private fun featureNotImplemented(){
        Toast.makeText(requireContext(), "Not implemented yet!", Toast.LENGTH_SHORT).show()
    }
    
}