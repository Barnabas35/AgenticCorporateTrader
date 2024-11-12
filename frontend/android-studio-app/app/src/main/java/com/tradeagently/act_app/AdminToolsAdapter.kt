package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class AdminToolsAdapter(
    private val tickets: List<SupportTicket>? = null,
    private val reviews: List<Review>? = null,
    private val users: List<User>? = null,  // Add users list
    private val message: String? = null,
    private val context: Context,
    private val onUserDeleteClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<AdminToolsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectText: TextView = view.findViewById(R.id.subjectText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        val deleteButton: Button? = view.findViewById(R.id.deleteButton) // For deleting a user if needed

        init {
            deleteButton?.setOnClickListener {
                users?.get(adapterPosition)?.let { user ->
                    onUserDeleteClick?.invoke(user.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when {
            message != null -> {
                holder.subjectText.text = message
                holder.descriptionText.text = ""
            }
            tickets != null -> {
                val ticket = tickets[position]
                holder.subjectText.text = ticket.issue_subject
                holder.descriptionText.text = ticket.issue_description
            }
            reviews != null -> {
                val review = reviews[position]
                holder.subjectText.text = "Score: ${review.score}"
                holder.descriptionText.text = review.comment
            }
            users != null -> {
                val user = users[position]
                holder.subjectText.text = "Username: ${user.username}"
                holder.descriptionText.text = "Email: ${user.email}"
                holder.deleteButton?.visibility = View.VISIBLE // Show delete button
            }
        }
    }

    override fun getItemCount(): Int {
        return when {
            message != null -> 1
            tickets != null -> tickets.size
            reviews != null -> reviews.size
            users != null -> users.size
            else -> 0
        }
    }
}
