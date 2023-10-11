package com.azmat.bytechat.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.azmat.bytechat.databinding.FragmentContactBinding
import com.azmat.bytechat.ui.adapters.ContactRecyclerViewAdapter
import com.azmat.bytechat.viewmodels.FirebaseViewModel

class ContactFragment : Fragment() {
   private lateinit var binding: FragmentContactBinding
   private lateinit var mViewModel: FirebaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = FirebaseViewModel()
        mViewModel.fetchUsers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentContactBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactRecyclerViewAdapter()
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter.onItemClick = {
            findNavController().navigate(ContactFragmentDirections.actionContactFragmentToMessageFragment(it))
        }

        binding.navigateUp.setOnClickListener {
            findNavController().navigateUp()
        }

        mViewModel.userList.observe(viewLifecycleOwner){
            if(it!=null){
                adapter.setData(it)
            }
        }
    }
}