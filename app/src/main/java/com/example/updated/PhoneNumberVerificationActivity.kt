package com.example.updated
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.chaos.view.PinView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class PhoneNumberVerificationActivity : AppCompatActivity() {
    private lateinit var etPhoneNum: TextInputEditText
//    private lateinit var etOtp: TextInputLayout
    private lateinit var btnGetOtp: MaterialButton
    private lateinit var btnskipOtp: MaterialButton
    private lateinit var btnVerify: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var otpView: PinView
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var phoneNum: String = "+91"
    private lateinit var codePicker: CountryCodePicker
    private lateinit var textAuthenticateNum: MaterialTextView
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var storedVerificationId:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_phone_number_verification)
        textAuthenticateNum = findViewById(R.id.text_authenticate_num)
        etPhoneNum = findViewById(R.id.et_phone_num)
        btnGetOtp = findViewById(R.id.btn_get_otp)
        btnVerify = findViewById(R.id.btn_verify)
        btnCancel = findViewById(R.id.btn_cancel)
        codePicker=findViewById(R.id.ccp)
        otpView = findViewById(R.id.otpview)
        btnskipOtp=findViewById(R.id.skip_otp)
        auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()
        setupEnteringPhoneNumberState()


    }

    private fun setupEnteringPhoneNumberState() {
       //AT THE BEGINNING ONLY SENDING OTP VIEW SHOULD BE VISIBLE ALL OTHER VIEWS SHOULD BE HIDDEN
        otpView.visibility = View.GONE
        btnVerify.visibility = View.GONE
        btnCancel.visibility = View.GONE
        btnGetOtp.visibility = View.VISIBLE
        textAuthenticateNum.visibility = View.GONE
        //GET OTP
        btnGetOtp.setOnClickListener {
            validateNumber()
            //CHANGING THE TEXT OF BUTTON FROM GETOTP TO OTPGENERATED
            btnGetOtp.text = "OTP Generated"
        }
        //SKIPPING THE REGISTRATION PROCESS BUT THEN THE SHARE AND DOWNLOAD BUTTON BECOMES INVISIBLE
        btnskipOtp.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            //THIS PUTEXTRA VARIABLE IS BECAUSE IN THE MAINACTIVITY THERE IS THE LOGIC FOR QR CODE GENERATION BUT
            //WE ARE USING AN INTENT WITH THE CONDITION  if ( !isVerificationCompleted) IF THIS VARIABLE IS NOT SET
            //MEANS AS THE CALL GOES TO THE MAINACTIVITY IT WILL REDIRECT IT TO THE LOGIN VIEW AGAIN
            intent.putExtra("isVerificationCompleted", true)
            //THE SECOND PUT EXTRA VAR KEY IS BECAUSE WE NEED TO IMPLEMENT THE FUNCTIONALITY IN MAIN WE ARE MAKING THE DOWNLOAD
            //AND SHARE BUTTONS INVISIBLE IF THE USER IS SKIPPING REGISTRATION
            intent.putExtra("key",true)
            startActivity(intent)
            finish()
        }
    }

    private fun setupVerifyingOTPState() {

        //WHEN VERIFYING THE OTP THE LOWER VIEW BECOMES VISIBLE
        otpView.visibility = View.VISIBLE
        btnVerify.visibility = View.VISIBLE
        btnCancel.visibility = View.VISIBLE
        btnGetOtp.visibility = View.VISIBLE
        btnskipOtp.visibility=View.GONE
        textAuthenticateNum.visibility = View.VISIBLE
        btnVerify.setOnClickListener {
          //IF OTP IS NOT EMPTY CHECK FOR IT'S VALIDITY
            if (otpView.text.toString().isNotEmpty()) {
                otpView.clearFocus()
                verifyVerificationCode(otpView.text.toString())
            } else {
                //IF EMPTY THEN ENTER THE NUMBER
                otpView.error = "Enter OTP 🤨"
                otpView.requestFocus()
                return@setOnClickListener
            }
        }

        btnCancel.setOnClickListener {
            setupEnteringPhoneNumberState()
            btnGetOtp.text = "Get OTP"
            //IF WITHOUT VERIFYING IF WE CLICK ON CANCEL THEN THE SKIP REGISTRATION BTN BECOMES VISIBLE
            btnskipOtp.visibility=View.VISIBLE
        }
        verificationCallbacks()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum)       // PHONE NUMBER TO VERIFY
            .setTimeout(60L, TimeUnit.SECONDS) // TIMEOUT AND UNIT
            .setActivity(this)                 // ACTIVITY (FOR CALLABCK BINDING)
            .setCallbacks(callbacks)          // ONVERFICATIONSTATECHANGECALLBACKS
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun validateNumber() {
        val phoneNumber = etPhoneNum?.text.toString()
         //LOGIC IF THE NUMBER IS EMPTY
        if (phoneNumber.isEmpty()) {
            etPhoneNum.error = "Enter your Phone Number"
            etPhoneNum.requestFocus()
            return
        }
        //THE ENTERED NUMBER SHOULD BE OF TEN DIGITS
        if (phoneNumber.length == 10) {
            //FETCHING THE COUNTRYCODE SELECTED FROM THE CODE PICKER
            val country_code: String = codePicker.selectedCountryCode
            // APPEND THE COUNTRY CODE AT THE BEGINNING
            val fullPhoneNumber = "+$country_code$phoneNumber"
            phoneNum=fullPhoneNumber
            setupVerifyingOTPState()
        } else {
            Toast.makeText(this, "Enter a 10-digit number", Toast.LENGTH_SHORT).show()
        }
    }
    private fun verificationCallbacks() {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // THIS CALLBACK WILL BE INVOKED IN TWO SITUATIONS:
                // 1 - INSTANT VERIFICATION. IN SOME CASES THE PHONE NUMBER CAN BE INSTANTLY
                //     VERIFIED WITHOUT NEEDING TO SEND OR ENTER A VERIFICATION CODE.
                // 2 - AUTO-RETRIEVAL. ON SOME DEVICES GOOGLE PLAY SERVICES CAN AUTOMATICALLY
                //     DETECT THE INCOMING VERIFICATION SMS AND PERFORM VERIFICATION WITHOUT
                //     USER ACTION.
                val code = credential.smsCode
                if (code != null) {
                    verifyVerificationCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // THIS CALLBACK IS INVOKED IN AN INVALID REQUEST FOR VERIFICATION IS MADE,
                // FOR INSTANCE IF THE THE PHONE NUMBER FORMAT IS NOT VALID.
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // INVALID REQUEST
                        Toast.makeText(
                            this@PhoneNumberVerificationActivity,
                            "Invalid request", Toast.LENGTH_SHORT
                        ).show()
                        setupEnteringPhoneNumberState()

                    }
                    is FirebaseTooManyRequestsException -> {
                        // THE SMS QUOTA FOR THE PROJECT HAS BEEN EXCEEDED
                        Toast.makeText(
                            this@PhoneNumberVerificationActivity,
                            "The SMS quota for the project has been exceeded",
                            Toast.LENGTH_SHORT
                        ).show()
                        setupEnteringPhoneNumberState()

                    }
                    else -> {
                        // SHOW A MESSAGE AND UPDATE THE UI
                        Toast.makeText(
                            this@PhoneNumberVerificationActivity,
                            e.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        setupEnteringPhoneNumberState()
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // THE SMS VERIFICATION CODE HAS BEEN SENT TO THE PROVIDED PHONE NUMBER, WE
                // NOW NEED TO ASK THE USER TO ENTER THE CODE AND THEN CONSTRUCT A CREDENTIAL
                // BY COMBINING THE CODE WITH A VERIFICATION ID.
                // SAVE VERIFICATION ID AND RESENDING TOKEN SO WE CAN USE THEM LATER
                storedVerificationId = verificationId
                resendToken = token

                Toast.makeText(
                    this@PhoneNumberVerificationActivity,
                    "OTP sent to $phoneNum",
                    Toast.LENGTH_SHORT
                ).show()

                super.onCodeSent(verificationId, resendToken)

            }
        }

    }

    private fun verifyVerificationCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification completed 🥳", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    //THE VARIABLE IS SET TRUE SO THAT THE MAINACTIVITY DOESN'T REDIRECT US HERE AGAIN
                    intent.putExtra("isVerificationCompleted", true)
                    //KEY VARAIABLE SET TO FLASE BECAUSE WE HAVE USED TO HIDE THE SHARE AND DOWNLOAD BUTTON
                    //WE HIDE THESE BUTTONS BECAUSE USER CAN SKIPREGISTRATION
                    intent.putExtra("key",false)
                    startActivity(intent)
                    finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            this,
                            "The verification code entered was invalid 🥺",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                    }
                    setupEnteringPhoneNumberState()
                }
            }
    }
}
