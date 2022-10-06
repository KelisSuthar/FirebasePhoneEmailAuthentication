package com.example.firebasephoneemailauthentication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasephoneemailauthentication.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    var verificationid = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSendEmail.setOnClickListener(this)
        binding.btnEmail.setOnClickListener(this)
        binding.btnSendPhone.setOnClickListener(this)
        binding.btnPhone.setOnClickListener(this)

        auth = Firebase.auth
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnEmail -> {

            }
            R.id.btnSendEmail -> {
                sendOTPEmail()
            }
            R.id.btnSendPhone -> {
                sendOTPPhone()
            }
            R.id.btnPhone -> {
                Log.e(
                    "PHONE_AUTH",
                    "GET CREDENTIALS " + PhoneAuthProvider.getCredential(
                        verificationid,
                        binding.edNumberOTP.text!!.trim().toString()
                    )
                )
                signInWithPhoneAuthCredential(
                    PhoneAuthProvider.getCredential(
                        verificationid,
                        binding.edNumberOTP.text!!.trim().toString()
                    )
                )
            }
        }
    }

    private fun sendOTPEmail() {

    }

    private fun sendOTPPhone() {
        FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(false)//To Disable Captcha Testing
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(
                binding.edNumber.text!!.trim().toString()
            )       // Phone number to verify
            .setTimeout(10L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this@MainActivity)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.e("PHONE_AUTH", "onVerificationCompleted    $p0")
//            signInWithPhoneAuthCredential(p0)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Log.e("PHONE_AUTH", "onVerificationFailed  $p0")
            if (p0 is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (p0 is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            Log.e("PHONE_AUTH", "ON_CODE_SND  $p0")
            Log.e("PHONE_AUTH", "ON_CODE_SND  $p1")
            verificationid = p0
            super.onCodeSent(p0, p1)
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("PHONE_AUTH", "signInWithCredential:success")
                    Toast.makeText(
                        applicationContext,
                        "Phone Authentication is successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = task.result?.user
                    Log.d("PHONE_AUTH", "signInWithCredential $user")
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("PHONE_AUTH", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        Firebase.auth.signOut()
    }
}