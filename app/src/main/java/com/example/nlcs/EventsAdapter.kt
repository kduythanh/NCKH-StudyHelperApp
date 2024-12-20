package com.example.nlcs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import java.text.SimpleDateFormat
import java.util.*

class EventsAdapter(private val events: List<Event>, private val activity: ReminderMenuActivityAPI) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val summary: TextView = view.findViewById(R.id.eventSummary)
        val time: TextView = view.findViewById(R.id.eventTime)
        val updateButton: ImageView = view.findViewById(R.id.EditReminderMenuItemButton)
        val deleteButton: ImageView = view.findViewById(R.id.deleteReminderMenuItemButton)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        // Lấy thời gian bắt đầu và kết thúc từ Event
        val startDateTime = event.start.dateTime ?: event.start.date
        val endDateTime = event.end.dateTime ?: event.end.date
        // Chuyển đổi DateTime sang String với định dạng mong muốn
        val formattedTime = formatEventTime(startDateTime, endDateTime)
        val participantsEmails = event.attendees?.joinToString(separator = ", ") { it.email } ?: ""
        holder.summary.text = event.summary
        holder.time.text = formattedTime
        // Xử lý nút xóa sự kiện
        holder.deleteButton.setOnClickListener {
            activity.showDeleteConfirmationDialog(event.id)
        }
        // Xử lý nút sửa sự kiện
        holder.updateButton.setOnClickListener {
            activity.showEditEventDialog(event.id, event.summary, startDateTime, endDateTime, participantsEmails)
        }
    }
    override fun getItemCount(): Int = events.size

    private fun formatEventTime(start: DateTime, end: DateTime): String {
        try {
            // Chuyển DateTime thành Date
            val startDate = Date(start.value)
            val endDate = Date(end.value)
            // Định dạng theo dd-MM-yyyy hh:mm
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            // Lấy ngày và giờ bắt đầu, kết thúc
            val startDateStr = dateFormat.format(startDate)
            val startTimeStr = timeFormat.format(startDate)
            val endDateStr = dateFormat.format(endDate)
            val endTimeStr = timeFormat.format(endDate)
            // Ghép lại thành định dạng mong muốn
            return "$startDateStr $startTimeStr - $endDateStr $endTimeStr"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Invalid time"
        }
    }
}