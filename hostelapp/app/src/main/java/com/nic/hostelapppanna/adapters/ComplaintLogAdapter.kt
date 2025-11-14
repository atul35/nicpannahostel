package com.nic.hostelapppanna.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nic.hostelapppanna.R
import com.nic.hostelapppanna.models.ComplaintLog

class ComplaintLogAdapter(private val logs: List<ComplaintLog>) : RecyclerView.Adapter<ComplaintLogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_complaint_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.statusTextView.text = "Status: ${log.status}"
        holder.actionByTextView.text = "By: ${log.actionBy}"
        holder.actionOnTextView.text = "On: ${log.actionOn}"

        if (log.remarks.isNullOrEmpty()) {
            holder.remarksTextView.visibility = View.GONE
        } else {
            holder.remarksTextView.visibility = View.VISIBLE
            holder.remarksTextView.text = "Remarks: ${log.remarks}"
        }
    }

    override fun getItemCount() = logs.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val remarksTextView: TextView = itemView.findViewById(R.id.remarksTextView)
        val actionByTextView: TextView = itemView.findViewById(R.id.actionByTextView)
        val actionOnTextView: TextView = itemView.findViewById(R.id.actionOnTextView)
    }
}