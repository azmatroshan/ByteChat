package com.azmat.bytechat.login

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider

class LoginViewModel: ViewModel() {
    private val authRepository = LoginRepository()

    fun signInWithPhoneNumber(phoneNumber: String, activity: FragmentActivity, callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        authRepository.signInWithPhoneNumber(phoneNumber, activity, callback)
    }


}