package com.example.nlcs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.calendar.model.Event

class EventsAdapter(private val events: List<Event>) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val summary: TextView = view.findViewById(R.id.eventSummary)
        val time: TextView = view.findViewById(R.id.eventTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.summary.text = event.summary
        holder.time.text = event.start.dateTime?.toStringRfc3339() ?: "No time available"
    }

    override fun getItemCount(): Int = events.size
}
