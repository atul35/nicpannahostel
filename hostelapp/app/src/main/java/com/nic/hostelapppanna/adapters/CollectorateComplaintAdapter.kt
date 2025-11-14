package com.nic.hostelapppanna.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nic.hostelapppanna.R
import com.nic.hostelapppanna.models.AdminComplaint
import com.nic.hostelapppanna.models.ComplaintLog
import com.nic.hostelapppanna.models.Config
import org.json.JSONObject

class CollectorateComplaintAdapter(private val context: Context, private val complaints: List<AdminComplaint>) : RecyclerView.Adapter<CollectorateComplaintAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_complaint, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val complaint = complaints[position]
        holder.complaintTextTextView.text = complaint.complaintText

        when (complaint.status) {
            "new" -> holder.cardView.setBackgroundResource(R.drawable.border_red)
            "in_progress" -> holder.cardView.setBackgroundResource(R.drawable.border_yellow)
            "resolved" -> holder.cardView.setBackgroundResource(R.drawable.border_green)
        }

        holder.infoIcon.setOnClickListener {
            showInfoDialog(complaint)
        }

        holder.historyIcon.setOnClickListener {
            showHistoryDialog(complaint)
        }

        holder.editIcon.setOnClickListener {
            showEditDialog(complaint)
        }
    }

    private fun showInfoDialog(complaint: AdminComplaint) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_complaint_info, null)
        val userNameTextView = dialogView.findViewById<TextView>(R.id.userNameTextView)
        val phoneTextView = dialogView.findViewById<TextView>(R.id.phoneTextView)
        val hostelNameTextView = dialogView.findViewById<TextView>(R.id.hostelNameTextView)
        val hostelLocationTextView = dialogView.findViewById<TextView>(R.id.hostelLocationTextView)
        val dateTextView = dialogView.findViewById<TextView>(R.id.dateTextView)

        userNameTextView.text = complaint.fullName
        phoneTextView.text = complaint.phoneNumber
        hostelNameTextView.text = complaint.hostelName
        hostelLocationTextView.text = complaint.hostelLocation
        dateTextView.text = complaint.createdAt

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showHistoryDialog(complaint: AdminComplaint) {
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


    private fun showEditDialog(complaint: AdminComplaint) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_complaint, null)
        val remarksEditText = dialogView.findViewById<EditText>(R.id.remarksEditText)
        val statusSpinner = dialogView.findViewById<Spinner>(R.id.statusSpinner)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val remarksErrorTextView = dialogView.findViewById<TextView>(R.id.remarksErrorTextView)

        val statusOptionsForDisplay = arrayOf("NEW", "In Progress", "Resolved")
        val statusOptionsForApi = arrayOf("new", "in_progress", "resolved")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, statusOptionsForDisplay)
        statusSpinner.adapter = adapter

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        submitButton.setOnClickListener {
            val remarks = remarksEditText.text.toString()
            if (remarks.isEmpty()) {
                remarksErrorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }
            remarksErrorTextView.visibility = View.GONE
            val selectedPosition = statusSpinner.selectedItemPosition
            val status = statusOptionsForApi[selectedPosition]
            addComplaintLog(complaint.complaintId, status, remarks)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addComplaintLog(complaintId: Int, status: String, remarks: String) {
        val url = "${Config.BASE_URL}?action=add_complaint_log"
        val queue = Volley.newRequestQueue(context)

        val params = JSONObject()
        params.put("complaint_id", complaintId)
        params.put("action_taken_by", 1) // Assuming a hardcoded user ID for the admin
        params.put("status", status)
        params.put("remarks", remarks)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                try {
                    val responseStatus = response.getString("status")
                    val message = response.getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }


    override fun getItemCount() = complaints.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val complaintTextTextView: TextView = itemView.findViewById(R.id.complaintTextTextView)
        val infoIcon: ImageView = itemView.findViewById(R.id.infoIcon)
        val historyIcon: ImageView = itemView.findViewById(R.id.historyIcon)
        val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
    }
}