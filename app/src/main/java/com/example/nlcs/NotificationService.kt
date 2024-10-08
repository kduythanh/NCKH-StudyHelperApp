package com.example.nlcs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.api.services.calendar.model.EventDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NotificationService(private val context: Context) {

    private val channelId = "reminder_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Event Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun sendNotification(eventTitle: String, eventStartTime: EventDateTime) {
        // Giả sử eventStartTime chứa thời gian dưới dạng chuỗi ISO 8601
        val dateTimeString = eventStartTime.dateTime.toString() // Cần thay đổi phù hợp với cách bạn lấy thời gian
        val zonedDateTime = ZonedDateTime.parse(dateTimeString) // Sử dụng ZonedDateTime để phân tích chuỗi
        // Định dạng lại thời gian theo dạng giờ:phút:giây
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime = zonedDateTime.format(formatter)
        val intent = Intent(context, ReminderMenuActivityAPI::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Sự kiện sắp diễn ra!")
            .setContentText("Sự kiện: $eventTitle sẽ diễn ra vào lúc $formattedTime!")
            .setSmallIcon(R.drawable.ic_ctu_logo) // Thay thế bằng icon của bạn
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)
    }
}
