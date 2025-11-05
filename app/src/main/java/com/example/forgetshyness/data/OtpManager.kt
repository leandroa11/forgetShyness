package com.example.forgetshyness.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpManager(private val auth: FirebaseAuth) {

    private var verificationId: String? = null

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (Exception) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    onVerificationFailed(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@OtpManager.verificationId = verificationId
                    onCodeSent(verificationId)
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        code: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val credential = verificationId?.let { id ->
            PhoneAuthProvider.getCredential(id, code)
        }

        if (credential != null) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess(task.result?.user?.uid ?: "")
                    } else {
                        task.exception?.let { onError(it) }
                    }
                }
        } else {
            onError(Exception("Verification ID no encontrado. No se puede verificar el código."))
        }
    }
}
