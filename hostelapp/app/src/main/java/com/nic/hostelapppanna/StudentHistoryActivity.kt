package com.nic.hostelapppanna

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.adapters.ComplaintAdapter
import com.nic.hostelapppanna.models.Complaint
import com.nic.hostelapppanna.models.Config

class StudentHistoryActivity : AppCompatActivity() {

    private lateinit var complaintsRecyclerView: RecyclerView
    private lateinit var noDataTextView: TextView
    private lateinit var progressBar: ProgressBar
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        userId = intent.getStringExtra("USER_ID")

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView)
        noDataTextView = findViewById(R.id.noDataTextView)
        progressBar = findViewById(R.id.progressBar)

        complaintsRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchComplaints()
    }

    private fun fetchComplaints() {
        if (userId == null) {
            Toast.makeText(this, "Could not get user ID. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        val url = "${Config.BASE_URL}?action=get_complaints_by_user&user_id=$userId"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val complaintsArray = response.getJSONArray("data")
                        if (complaintsArray.length() > 0) {
                            val complaints = mutableListOf<Complaint>()
                            for (i in 0 until complaintsArray.length()) {
                                val complaintObject = complaintsArray.getJSONObject(i)
                                val complaint = Complaint(
                                    complaintObject.getInt("complaint_id"),
                                    complaintObject.getString("complaint_text"),
                                    complaintObject.getInt("user_id"),
                                    complaintObject.getString("created_on"),
                                    complaintObject.getString("status")
                                )
                                complaints.add(complaint)
                            }
                            complaintsRecyclerView.adapter = ComplaintAdapter(this, complaints)
                        } else {
                            noDataTextView.visibility = View.VISIBLE
                        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}