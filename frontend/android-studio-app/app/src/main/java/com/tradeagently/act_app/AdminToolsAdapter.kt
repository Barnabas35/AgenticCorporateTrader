package com.tradeagently.act_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
                tickets?.get(adapterPosition)?.let { ticket ->
                    onTicketClick?.invoke(
                        ticket.issue_subject,
                        ticket.issue_description,
                        ticket.user_id,
                        ticket.issue_status,
                        ticket.ticket_id,
                        ticket.unix_timestamp
                    )
                }

                reviews?.get(adapterPosition)?.let { review ->
                    onReviewClick?.invoke(review.score, review.comment)
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
