@file:Suppress("DEPRECATION")

package com.azmat.bytechat.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azmat.bytechat.databinding.FragmentProfileBinding
import com.azmat.bytechat.models.User
import com.azmat.bytechat.utils.ResultState
import com.azmat.bytechat.utils.buildDialog
import com.azmat.bytechat.utils.usersPath
import com.azmat.bytechat.viewmodels.FirebaseViewModel
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mViewModel: FirebaseViewModel
    private lateinit var imgProfile: ImageView
    private lateinit var dialog: ProgressDialog
    private var imgProfileUrl = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = FirebaseViewModel()

        dialog = buildDialog(requireContext(), "Uploading image")

        mViewModel.db.getReference(usersPath).child(mViewModel.auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object :
            ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        binding.name.setText(user?.name)
                        if(user?.imgProfile!=""){
                            imgProfileUrl = true
                            Glide.with(requireContext())
                                .load(user?.imgProfile)
                                .centerCrop()
                                .dontAnimate()
                                .into(binding.profileImageView)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        binding.navigateUp.setOnClickListener {
            if(findNavController().currentBackStack.value.isNotEmpty()){
                findNavController().navigateUp()
            }
        }

        binding.saveBtn.setOnClickListener {
            val user: User = if(imgProfileUrl){
                User(
                    mViewModel.auth.currentUser?.uid!!,
                    binding.name.text.toString(),
                    mViewModel.auth.currentUser?.phoneNumber!!,
                    "https://firebasestorage.googleapis.com/v0/b/bytechat-d79ed.appspot.com/o/profile%2Fprofile-circle-svgrepo-com%20(1).png?alt=media&token=3c32e052-5162-4714-a320-a7ed5adf130e"// Default
                )
            } else{
                User(
                    mViewModel.auth.currentUser?.uid!!,
                    binding.name.text.toString(),
                    mViewModel.auth.currentUser?.phoneNumber!!
                )
            }
            mViewModel.updateUser(user)
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToChatFragment())
        }

        imgProfile = binding.profileImageView
        binding.editImage.setOnClickListener {
            if(allPermissionsGranted()){
                getImage()
            }else{
                requestPermissions()
                if(allPermissionsGranted()){
                    getImage()
                }
            }
        }
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        val options = arrayOf<CharSequence>("Camera", "Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose")
        builder.setItems(options) { _, item ->
            when {
                options[item] == "Camera" -> {
                    getPreviewImage.launch(null)
                }
                options[item] == "Gallery" -> {
                    startForProfileImageResult.launch(intent)
                }
            }
        }
        builder.show()
    }

    private val getPreviewImage = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){ image->
        mViewModel.updateProfilePhoto(image!!)
        mViewModel.result.observe(viewLifecycleOwner){resultState->
            if(resultState!=null){
                when(resultState){
                    is ResultState.Loading -> {dialog.show()}
                    is ResultState.Success -> {
                        imgProfileUrl = true
                        dialog.dismiss()
                        Glide.with(this)
                            .load(image)
                            .centerCrop()
                            .into(imgProfile)
                        Toast.makeText(requireContext(), resultState.data, Toast.LENGTH_SHORT).show()
                    }
                    is ResultState.Error -> {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), resultState.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    mViewModel.updateProfilePhoto(fileUri)
                    mViewModel.result.observe(viewLifecycleOwner){resultState->
                        if(resultState!=null){
                            when(resultState){
                                is ResultState.Loading -> {dialog.show()}
                                is ResultState.Success -> {
                                    imgProfileUrl = true
                                    dialog.dismiss()
                                    Glide.with(this)
                                        .load(fileUri)
                                        .centerCrop()
                                        .into(imgProfile)
                                    Toast.makeText(requireContext(), resultState.data, Toast.LENGTH_SHORT).show()
                                }
                                is ResultState.Error -> {
                                    dialog.dismiss()
                                    Toast.makeText(requireContext(), resultState.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

    companion object{
        private const val PERMISSION_REQUEST_CODE = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).apply {}.toTypedArray()
    }
}