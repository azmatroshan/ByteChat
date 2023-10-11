@file:Suppress("DEPRECATION")

package com.azmat.bytechat.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.azmat.bytechat.databinding.FragmentLoginBinding
import com.azmat.bytechat.utils.buildDialog
import com.azmat.bytechat.viewmodels.FirebaseViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mViewModel: FirebaseViewModel
    private lateinit var dialog: ProgressDialog

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p: PhoneAuthCredential) {
            Log.d(LOGIN, "Verification Completed: ${p.smsCode}")
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            Log.e(LOGIN, "Verification failed: ${exception.message}")
            Toast.makeText(requireContext(), "Verification failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            val action = LoginFragmentDirections.actionLoginFragmentToOtpFragment(verificationId)
            dialog.dismiss()
            findNavController().navigate(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser!=null){
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToChatFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ccp = binding.countryCodePicker
        val number = binding.phoneNumber
        ccp.registerCarrierNumberEditText(number)
        dialog = buildDialog(requireContext(), "Sending OTP")

        binding.sendOtp.setOnClickListener{
            val enteredNumber = ccp.fullNumberWithPlus
            if(ccp.isValidFullNumber){
                Log.d("Number", enteredNumber)
                dialog.show()
                hideKeyboard()
                mViewModel.signInWithPhoneNumber(enteredNumber, requireActivity(), callbacks)
            } else{
                Toast.makeText(requireContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        const val LOGIN = "Login"
    }
}