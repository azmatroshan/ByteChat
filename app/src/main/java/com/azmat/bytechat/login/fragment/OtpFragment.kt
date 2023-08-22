package com.azmat.bytechat.login.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azmat.bytechat.databinding.FragmentOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OtpFragment : Fragment() {
    private lateinit var binding: FragmentOtpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentOtpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        var verificationId: String? = null
        arguments?.let {
            val args = OtpFragmentArgs.fromBundle(it)
            verificationId = args.verificationCode
        }
        binding.btnVerify.setOnClickListener {
            val code = binding.pinview.value
            if(code.isEmpty()){
                Toast.makeText(requireContext(), "Please enter OTP", Toast.LENGTH_SHORT).show()
            }
            else if(!verificationId.isNullOrEmpty()){
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithCredential:success")
                findNavController().navigate(OtpFragmentDirections.actionOtpFragmentToChatFragment())
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    companion object {
        const val TAG = "OTP"
    }
}