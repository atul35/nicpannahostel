package com.nic.hostelapppanna

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.models.Config
import org.json.JSONObject

class ComplaintActivity : AppCompatActivity() {

    private lateinit var editTextComplain: EditText
    private lateinit var charCounter: TextView
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint)

        userId = intent.getStringExtra("USER_ID")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editTextComplain = findViewById(R.id.editTextComplain)
        charCounter = findViewById(R.id.char_counter)
        submitButton = findViewById(R.id.submit_button)
        progressBar = findViewById(R.id.progressBar)

        editTextComplain.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                charCounter.text = "$length/1000"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        submitButton.setOnClickListener {
            if (editTextComplain.text.length >= 50) {
                addComplaint()
            } else {
                Toast.makeText(this, "Please enter at least 50 characters", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addComplaint() {
        if (userId == null) {
            Toast.makeText(this, "Could not get user ID. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        val url = "${Config.BASE_URL}?action=add_complaint"
        val queue = Volley.newRequestQueue(this)

        val params = JSONObject()
        params.put("complaint_text", editTextComplain.text.toString())
        params.put("user_id", userId)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                progressBar.visibility = View.GONE
                try {
                    val status = response.getString("status")
                    val message = response.getString("message")
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (status == "success") {
                        finish()
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}