package com.nic.hostelapppanna

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.models.Config
import com.nic.hostelapppanna.models.User
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var hostelNameTextView: TextView
    private lateinit var hostelLocationTextView: TextView
    private lateinit var logoutButton: Button
    private var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        phoneNumber = intent.getStringExtra("PHONE_NUMBER")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        roleTextView = findViewById(R.id.roleTextView)
        hostelNameTextView = findViewById(R.id.hostelNameTextView)
        hostelLocationTextView = findViewById(R.id.hostelLocationTextView)
        logoutButton = findViewById(R.id.logout_button)

        fetchUserProfile()

        logoutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserProfile() {
        if (phoneNumber == null) {
            Toast.makeText(this, "Could not get phone number. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "${Config.BASE_URL}?action=get_user_by_phone&phone_number=$phoneNumber"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val userData = response.getJSONObject("data")
                    val user = User(
                        userData.getString("user_id"),
                        userData.getString("full_name"),
                        userData.getString("email"),
                        userData.getString("phone_number"),
                        userData.getString("role"),
                        userData.getString("hostel_id"),
                        userData.getString("hostel_name"),
                        userData.getString("hostel_location")
                    )
                    displayUserProfile(user)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error fetching user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun displayUserProfile(user: User) {
        nameTextView.text = user.fullName
        emailTextView.text = user.email
        phoneTextView.text = user.phoneNumber
        roleTextView.text = user.role
        hostelNameTextView.text = user.hostelName
        hostelLocationTextView.text = user.hostelLocation
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}