package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminToolsAdapter(
    private val tickets: List<SupportTicket>? = null,
    private val reviews: List<Review>? = null,
    private val users: List<User>? = null,
    private val context: Context,
    private val onUserDeleteClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<AdminToolsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectText: TextView = view.findViewById(R.id.subjectText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when {
            tickets != null -> {
                val ticket = tickets[position]
                holder.subjectText.text = ticket.issue_subject
                holder.descriptionText.text = ticket.issue_description
                holder.deleteButton.visibility = View.GONE

                // Set click listener to navigate to TicketDetailsActivity
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, TicketDetailsActivity::class.java).apply {
                        putExtra("issue_subject", ticket.issue_subject)
                        putExtra("user_id", ticket.user_id)
                        putExtra("issue_description", ticket.issue_description)
                        putExtra("issue_status", ticket.issue_status)
                        putExtra("ticket_id", ticket.ticket_id)
                    }
                    context.startActivity(intent)
                }
            }
            reviews != null -> {
                val review = reviews[position]
                holder.subjectText.text = "Score: ${review.score}"
                holder.descriptionText.text = review.comment
                holder.deleteButton.visibility = View.GONE

                // Set click listener to navigate to ReviewDetailsActivity
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, ReviewDetailsActivity::class.java).apply {
                        putExtra("score", review.score)
                        putExtra("comment", review.comment)
                    }
                    context.startActivity(intent)
                }
            }
            users != null -> {
                val user = users[position]
                holder.subjectText.text = "Username: ${user.username}"
                holder.descriptionText.text = "Email: ${user.email}"
                holder.deleteButton.visibility = View.VISIBLE

                // Handle delete button click
                holder.deleteButton.setOnClickListener {
                    onUserDeleteClick?.invoke(user.client_id)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return when {
            tickets != null -> tickets.size
            reviews != null -> reviews.size
            users != null -> users.size
            else -> 0
        }
    }
}
