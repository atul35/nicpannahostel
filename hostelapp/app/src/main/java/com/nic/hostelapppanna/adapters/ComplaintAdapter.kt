package com.nic.hostelapppanna.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.R
import com.nic.hostelapppanna.models.Complaint
import com.nic.hostelapppanna.models.ComplaintLog
import com.nic.hostelapppanna.models.Config

class ComplaintAdapter(private val context: Context, private val complaints: List<Complaint>) : RecyclerView.Adapter<ComplaintAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val complaint = complaints[position]
        holder.complaintTextTextView.text = complaint.complaintText
        holder.dateTextView.text = complaint.createdAt
        holder.statusTextView.text = "Status: ${complaint.status}"

        holder.historyIcon.setOnClickListener {
            showHistoryDialog(complaint)
        }
    }

    private fun showHistoryDialog(complaint: Complaint) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_complaint_history, null)
        val logsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.logsRecyclerView)
        val noLogsTextView = dialogView.findViewById<TextView>(R.id.noLogsTextView)
        logsRecyclerView.layoutManager = LinearLayoutManager(context)

        val url = "${Config.BASE_URL}?action=get_complaint_logs_by_complain&complaint_id=${complaint.complaintId}"
        val queue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val logsArray = response.getJSONArray("data")
                        if (logsArray.length() > 0) {
                            val logs = mutableListOf<ComplaintLog>()
                            for (i in 0 until logsArray.length()) {
                                val logObject = logsArray.getJSONObject(i)
                                val log = ComplaintLog(
                                    logObject.getString("status"),
                                    logObject.getString("remarks"),
                                    logObject.getString("action_on"),
                                    logObject.getString("action_by")
                                )
                                logs.add(log)
                            }
                            logsRecyclerView.adapter = ComplaintLogAdapter(logs)
                        } else {
                            logsRecyclerView.visibility = View.GONE
                            noLogsTextView.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error parsing logs", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error fetching logs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun getItemCount() = complaints.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val complaintTextTextView: TextView = itemView.findViewById(R.id.complaintTextTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val historyIcon: ImageView = itemView.findViewById(R.id.historyIcon)
    }
}