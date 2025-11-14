package com.nic.hostelapppanna

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.models.Config

class MainActivity : AppCompatActivity() {

    private lateinit var edtPhone: EditText
    private lateinit var edtOTP: EditText
    private lateinit var verifyOTPBtn: Button
    private lateinit var generateOTPBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var wrongOtpTextView: TextView
    private lateinit var resendLayout: LinearLayout
    private lateinit var resendOtpTextView: TextView
    private lateinit var countdownTextView: TextView
    private lateinit var resendAttemptsTextView: TextView

    private var resendCounter = 5
    private var verifyAttempts = 5
    private var timer: CountDownTimer? = null
    private var currentUserId: String? = null
    private var currentPhoneNumber: String? = null
    private var currentUserRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtPhone = findViewById(R.id.idEdtPhoneNumber)
        edtOTP = findViewById(R.id.idEdtOtp)
        verifyOTPBtn = findViewById(R.id.idBtnVerify)
        generateOTPBtn = findViewById(R.id.idBtnGetOtp)
        progressBar = findViewById(R.id.progressBar)
        wrongOtpTextView = findViewById(R.id.wrongOtpTextView)
        resendLayout = findViewById(R.id.resendLayout)
        resendOtpTextView = findViewById(R.id.resendOtpTextView)
        countdownTextView = findViewById(R.id.countdownTextView)
        resendAttemptsTextView = findViewById(R.id.resendAttemptsTextView)

        generateOTPBtn.setOnClickListener {
            handleOtpRequest()
        }

        verifyOTPBtn.setOnClickListener {
            if (edtOTP.text.toString() == "999999") {
                if (currentUserId != null && currentPhoneNumber != null && currentUserRole != null) {
                    if (currentUserRole == "student") {
                        val i = Intent(this@MainActivity, StudentHomeActivity::class.java)
                        i.putExtra("USER_ID", currentUserId)
                        i.putExtra("PHONE_NUMBER", currentPhoneNumber)
                        startActivity(i)
                        finish()
                    } else if (currentUserRole == "collectorate") {
                        val i = Intent(this@MainActivity, CollectorateHomeActivity::class.java)
                        i.putExtra("PHONE_NUMBER", currentPhoneNumber)
                        startActivity(i)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Could not get user details. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                verifyAttempts--
                if (verifyAttempts > 0) {
                    wrongOtpTextView.text = "Incorrect OTP. $verifyAttempts/5 attempts left"
                    wrongOtpTextView.visibility = View.VISIBLE
                    resendLayout.visibility = View.VISIBLE
                    if (resendCounter > 0) {
                        startResendTimer()
                    } else {
                        resendOtpTextView.text = "Maximum retries reached"
                        countdownTextView.visibility = View.GONE
                        resendAttemptsTextView.visibility = View.GONE
                    }
                } else {
                    wrongOtpTextView.text = "Maximum verification attempts reached."
                    verifyOTPBtn.isEnabled = false
                    resendLayout.visibility = View.GONE
                }
            }
        }

        resendOtpTextView.setOnClickListener {
            if (resendOtpTextView.isClickable) {
                handleOtpRequest()
            }
        }
    }

    private fun handleOtpRequest() {
        if (resendCounter <= 0) {
            Toast.makeText(this, "Maximum OTP requests reached", Toast.LENGTH_SHORT).show()
            return
        }

        if (edtPhone.text.length == 10) {
            checkUserExists(edtPhone.text.toString())
        } else {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startResendTimer() {
        resendOtpTextView.isClickable = false
        timer?.cancel() // Cancel any existing timer
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownTextView.text = "in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                if (resendCounter > 0) {
                    resendOtpTextView.isClickable = true
                    countdownTextView.text = ""
                } else {
                    resendOtpTextView.text = "Maximum retries reached"
                    countdownTextView.visibility = View.GONE
                    resendAttemptsTextView.visibility = View.GONE
                }
            }
        }.start()
    }

    private fun checkUserExists(phoneNumber: String) {
        progressBar.visibility = View.VISIBLE
        val url = "${Config.BASE_URL}?action=get_user_by_phone&phone_number=$phoneNumber"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        resendCounter-- // Consume one attempt
                        val userData = response.getJSONObject("data")
                        currentUserId = userData.getString("user_id")
                        currentPhoneNumber = phoneNumber
                        currentUserRole = userData.getString("role")

                        edtOTP.visibility = View.VISIBLE
                        verifyOTPBtn.visibility = View.VISIBLE
                        wrongOtpTextView.visibility = View.GONE
                        resendLayout.visibility = View.VISIBLE
                        resendAttemptsTextView.text = "($resendCounter/5 attempts left)"
                        resendAttemptsTextView.visibility = View.VISIBLE
                        startResendTimer()
                        Toast.makeText(this, "OTP is sent", Toast.LENGTH_SHORT).show()

                        if (resendCounter <= 0) {
                            generateOTPBtn.isEnabled = false
                            resendOtpTextView.text = "Maximum retries reached"
                            resendOtpTextView.isClickable = false
                            countdownTextView.visibility = View.GONE
                            resendAttemptsTextView.visibility = View.GONE
                        }
                    } else {
                        showUserNotRegisteredDialog()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun showUserNotRegisteredDialog() {
        AlertDialog.Builder(this)
            .setTitle("User Not Registered")
            .setMessage("Please contact hostel admin to register yourself.")
            .setPositiveButton("OK", null)
            .show()
    }
}