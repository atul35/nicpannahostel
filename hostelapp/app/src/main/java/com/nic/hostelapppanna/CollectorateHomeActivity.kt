package com.nic.hostelapppanna

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.adapters.CollectorateComplaintAdapter
import com.nic.hostelapppanna.models.AdminComplaint
import com.nic.hostelapppanna.models.Config

class CollectorateHomeActivity : AppCompatActivity() {

    private lateinit var complaintsRecyclerView: RecyclerView
    private lateinit var noDataTextView: TextView
    private lateinit var progressBar: ProgressBar
    private var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collectorate_home)

        phoneNumber = intent.getStringExtra("PHONE_NUMBER")

        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView)
        noDataTextView = findViewById(R.id.noDataTextView)
        progressBar = findViewById(R.id.progressBar)

        complaintsRecyclerView.layoutManager = GridLayoutManager(this, 2) // Using a 2-column grid

        fetchComplaints()
    }

    private fun fetchComplaints() {
        progressBar.visibility = View.VISIBLE
        val url = "${Config.BASE_URL}?action=get_complaints"
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
                            val complaints = mutableListOf<AdminComplaint>()
                            for (i in 0 until complaintsArray.length()) {
                                val complaintObject = complaintsArray.getJSONObject(i)
                                val complaint = AdminComplaint(
                                    complaintObject.getInt("complaint_id"),
                                    complaintObject.getString("complaint_text"),
                                    complaintObject.getString("full_name"),
                                    complaintObject.getString("phone_number"),
                                    complaintObject.getString("hostel_name"),
                                    complaintObject.getString("hostel_location"),
                                    complaintObject.getString("status"),
                                    complaintObject.getString("created_on")
                                )
                                complaints.add(complaint)
                            }
                            complaintsRecyclerView.adapter = CollectorateComplaintAdapter(this, complaints)
                        } else {
                            noDataTextView.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(jsonObjectRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("PHONE_NUMBER", phoneNumber)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}