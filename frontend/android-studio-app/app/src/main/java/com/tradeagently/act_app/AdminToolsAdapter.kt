package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class AdminToolsAdapter(
    private val tickets: List<SupportTicket>?,
    private val reviews: List<Review>?,
    private val onTicketClick: ((String, String, String, String, String, Long) -> Unit)? = null,
    private val onReviewClick: ((Int, String) -> Unit)? = null
) : RecyclerView.Adapter<AdminToolsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectText: TextView = view.findViewById(R.id.subjectText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)

        init {
            view.setOnClickListener {
                val context = view.context

                tickets?.get(adapterPosition)?.let { ticket ->
                    onTicketClick?.invoke(
                        ticket.issue_subject,
                        ticket.issue_description,
                        ticket.user_id,
                        ticket.issue_status,
                        ticket.ticket_id,
                        ticket.unix_timestamp
                    )
                    val intent = Intent(context, TicketDetailsActivity::class.java)
                    intent.putExtra("issue_subject", ticket.issue_subject)
                    intent.putExtra("issue_description", ticket.issue_description)
                    intent.putExtra("user_id", ticket.user_id)
                    intent.putExtra("issue_status", ticket.issue_status)
                    intent.putExtra("ticket_id", ticket.ticket_id)
                    intent.putExtra("unix_timestamp", ticket.unix_timestamp)
                    context.startActivity(intent)
                    (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
                }

                reviews?.get(adapterPosition)?.let { review ->
                    onReviewClick?.invoke(review.score, review.comment)
                    val intent = Intent(context, ReviewDetailsActivity::class.java)
                    intent.putExtra("score", review.score)
                    intent.putExtra("comment", review.comment)
                    context.startActivity(intent)
                    (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (tickets != null) {
            val ticket = tickets[position]
            holder.subjectText.text = ticket.issue_subject
            holder.descriptionText.text = ticket.issue_description
        } else if (reviews != null) {
            val review = reviews[position]
            holder.subjectText.text = "Score: ${review.score}"
            holder.descriptionText.text = review.comment
        }
    }

    override fun getItemCount(): Int {
        return tickets?.size ?: reviews?.size ?: 0
    }
}
