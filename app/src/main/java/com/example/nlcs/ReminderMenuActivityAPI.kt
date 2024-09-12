package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReminderMenuActivityAPI : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var credential: GoogleAccountCredential
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_menu_api)

        // Cấu hình Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Cấu hình RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Thoát khỏi Google Sign-In, đăng nhập lại
        signOut()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Đăng xuất thành công, bắt đầu quá trình đăng nhập lại
            signIn()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(CalendarScopes.CALENDAR)
            ).setSelectedAccount(account?.account)

            // Lấy dữ liệu từ Google Calendar
            fetchCalendarEvents()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun fetchCalendarEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()

            // Tính toán thời gian bắt đầu và kết thúc của tuần
            val now = DateTime(System.currentTimeMillis())
            val startOfWeek = DateTime(System.currentTimeMillis() - (now.value % (7 * 24 * 60 * 60 * 1000)))
            val endOfWeek = DateTime(startOfWeek.value + (7 * 24 * 60 * 60 * 1000) - 1)

            val events = try {
                val eventsResponse: Events = calendarService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(startOfWeek)
                    .setTimeMax(endOfWeek)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                eventsResponse.items
            } catch (e: Exception) {
                Log.e("CalendarError", "Error fetching events: ${e.localizedMessage}")
                emptyList()
            }

            withContext(Dispatchers.Main) {
                // Cập nhật giao diện người dùng với dữ liệu mới ở đây
                val adapter = EventsAdapter(events)
                recyclerView.adapter = adapter
            }
        }
    }

    private fun getReminders() {
        val calendarService = Calendar.Builder(
            NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential
        ).setApplicationName("Reminder App").build()

        val now = DateTime(System.currentTimeMillis())
        val events = calendarService.events().list("primary")
            .setMaxResults(100)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()

        val items = events.items
        for (event in items) {
            Log.d(TAG, "Event: ${event.summary}, Time: ${event.start.dateTime}")
        }
    }

    fun addReminder(summary: String, description: String, startTime: DateTime, endTime: DateTime) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()

            val event = Event()
                .setSummary(summary)
                .setDescription(description)
                .setStart(EventDateTime().setDateTime(startTime))
                .setEnd(EventDateTime().setDateTime(endTime))

            try {
                calendarService.events().insert("primary", event).execute()
            } catch (e: Exception) {
                Log.e("CalendarError", "Error adding event: ${e.localizedMessage}")
            }
        }
    }

    fun updateReminder(eventId: String, newSummary: String, newDescription: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()

            try {
                val event = calendarService.events().get("primary", eventId).execute()
                event.summary = newSummary
                event.description = newDescription

                calendarService.events().update("primary", eventId, event).execute()
            } catch (e: Exception) {
                Log.e("CalendarError", "Error updating event: ${e.localizedMessage}")
            }
        }
    }

    fun deleteReminder(eventId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()

            try {
                calendarService.events().delete("primary", eventId).execute()
            } catch (e: Exception) {
                Log.e("CalendarError", "Error deleting event: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1001
        private const val TAG = "ReminderMenuActivityAPI"
    }
}
