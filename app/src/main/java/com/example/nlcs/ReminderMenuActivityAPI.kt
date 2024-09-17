package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar as JavaCalendar

class ReminderMenuActivityAPI : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var credential: GoogleAccountCredential
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var previousWeekButton: ImageButton
    private lateinit var nextWeekButton: ImageButton
    private lateinit var addButton: FloatingActionButton
    private lateinit var exitButton: ImageButton
    private var currentWeekStartDate: JavaCalendar = JavaCalendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_menu_api)
        // Cấu hình Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Xử lý nút thoát khỏi chức năng
        exitButton = findViewById(R.id.exitButton)
        exitButton.setOnClickListener {
            finish()
        }
        // Cấu hình RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Cấu hình SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadEventsForSelectedWeek()
        }
        previousWeekButton = findViewById(R.id.previousWeekButton)
        // Button chuyển về tuần trước
        previousWeekButton.setOnClickListener {
            currentWeekStartDate.add(JavaCalendar.DATE, -7) // Trừ 7 ngày để chuyển về tuần trước
            loadEventsForSelectedWeek()
        }
        nextWeekButton = findViewById(R.id.nextWeekButton)
        // Button chuyển về tuần sau
        nextWeekButton.setOnClickListener {
            currentWeekStartDate.add(JavaCalendar.DATE, 7) // Cộng 7 ngày để chuyển về tuần sau
            loadEventsForSelectedWeek()
        }
        // Button thêm nhắc nhở
        addButton = findViewById(R.id.reminderAddButton)
        addButton.setOnClickListener {
            showAddEventDialog()
        }
        // Thoát khỏi Google Sign-In, đăng nhập lại
        signOut()
    }
    // Load danh sách các sự kiện trong tuần được chọn
    private fun loadEventsForSelectedWeek() {
        val startOfWeek = currentWeekStartDate.clone() as JavaCalendar
        startOfWeek.set(JavaCalendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek) // Ngày bắt đầu tuần
        val endOfWeek = currentWeekStartDate.clone() as JavaCalendar
        endOfWeek.set(JavaCalendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek + 6) // Ngày kết thúc tuần
        // Gọi hàm fetchCalendarEvents với ngày bắt đầu và kết thúc
        fetchCalendarEvents(startOfWeek.time, endOfWeek.time)
    }
    // Hiển thị menu thêm sự kiện
    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_event_layout, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            val saveButton = dialog.findViewById<Button>(R.id.saveEventButton)
            val cancelButton = dialog.findViewById<Button>(R.id.cancelEventButton)
            saveButton?.setOnClickListener {
                val eventTitle = dialog.findViewById<EditText>(R.id.eventTitle)?.text.toString()
                val startDate = dialog.findViewById<EditText>(R.id.startDate)?.text.toString()
                val startTime = dialog.findViewById<EditText>(R.id.startTime)?.text.toString()
                val endDate = dialog.findViewById<EditText>(R.id.endDate)?.text.toString()
                val endTime = dialog.findViewById<EditText>(R.id.endTime)?.text.toString()
                if (validateEventInput(eventTitle, startDate, startTime, endDate, endTime)) {
                    val startDateTime = parseDateTime(startDate, startTime)
                    val endDateTime = parseDateTime(endDate, endTime)
                    addReminder(eventTitle, startDateTime, endDateTime)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sự kiện", Toast.LENGTH_SHORT).show()
                }
            }
            cancelButton?.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    // Hiển thị menu chỉnh sửa thông tin sự kiện
    fun showEditEventDialog(eventId: String, currentTitle: String, currentStartDate: DateTime, currentEndDate: DateTime) {
        val dialogView = layoutInflater.inflate(R.layout.edit_event_layout, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.setOnShowListener {
            val titleEditText = dialog.findViewById<EditText>(R.id.eventTitleEditText)
            val startDateEditText = dialog.findViewById<EditText>(R.id.startDateEditText)
            val startTimeEditText = dialog.findViewById<EditText>(R.id.startTimeEditText)
            val endDateEditText = dialog.findViewById<EditText>(R.id.endDateEditText)
            val endTimeEditText = dialog.findViewById<EditText>(R.id.endTimeEditText)
            val saveButton = dialog.findViewById<Button>(R.id.saveEditedEventButton)
            val cancelButton = dialog.findViewById<Button>(R.id.cancelEditedEventButton)
            // Thiết lập dữ liệu hiện tại cho các trường
            titleEditText?.setText(currentTitle)
            startDateEditText?.setText(formatDate(currentStartDate))
            startTimeEditText?.setText(formatTime(currentStartDate))
            endDateEditText?.setText(formatDate(currentEndDate))
            endTimeEditText?.setText(formatTime(currentEndDate))
            saveButton?.setOnClickListener {
                val newTitle = titleEditText?.text.toString()
                val newStartDate = startDateEditText?.text.toString()
                val newStartTime = startTimeEditText?.text.toString()
                val newEndDate = endDateEditText?.text.toString()
                val newEndTime = endTimeEditText?.text.toString()
                if (validateEventInput(newTitle, newStartDate, newStartTime, newEndDate, newEndTime)) {
                    val newStartDateTime = parseDateTime(newStartDate, newStartTime)
                    val newEndDateTime = parseDateTime(newEndDate, newEndTime)
                    updateReminder(eventId, newTitle, newStartDateTime, newEndDateTime)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sự kiện", Toast.LENGTH_SHORT).show()
                }
            }
            cancelButton?.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    // Hàm này sẽ format DateTime thành String để hiển thị ngày
    private fun formatDate(dateTime: DateTime): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date(dateTime.value)
        return dateFormat.format(date)
    }
    // Hàm này sẽ format DateTime thành String để hiển thị giờ
    private fun formatTime(dateTime: DateTime): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(dateTime.value)
        return timeFormat.format(date)
    }
    // Kiểm tra ngày nhập vào đã hợp lệ chưa
    private fun validateEventInput(title: String, startDate: String, startTime: String, endDate: String, endTime: String): Boolean {
        return title.isNotEmpty() && startDate.isNotEmpty() && startTime.isNotEmpty() && endDate.isNotEmpty() && endTime.isNotEmpty()
    }
    // Chuyển đổi định dạng ngày tháng năm theo quy định
    private fun parseDateTime(date: String, time: String): DateTime {
        // Định dạng ngày/giờ theo định dạng của người dùng
        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        // Định dạng ngày/giờ theo chuẩn ISO 8601
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateTimeString = "$date $time"
        val dateTime = inputFormat.parse(dateTimeString) ?: throw IllegalArgumentException("Invalid date/time format")
        return DateTime(outputFormat.format(dateTime))
    }
    // Sign in
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }
    // Sign out
    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            signIn() // Đăng xuất thành công, bắt đầu quá trình đăng nhập lại
        }
    }
    @Deprecated("Deprecated in Java")
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
            loadEventsForSelectedWeek()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }
    // Lấy danh sách sự kiện từ Google Calendar
    private fun fetchCalendarEvents(startDate: Date, endDate: Date) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            // Chuyển đổi startDate và endDate thành định dạng DateTime (ISO 8601)
            val timeMin = DateTime(startDate)
            val timeMax = DateTime(endDate)
            val events = try {
                val eventsResponse: Events = calendarService.events().list("primary")
                    .setMaxResults(20)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                eventsResponse.items
            } catch (e: Exception) {
                Log.e("CalendarError", "Error fetching events: ${e.localizedMessage}")
                emptyList()
            }
            withContext(Dispatchers.Main) {
                val adapter = EventsAdapter(events, this@ReminderMenuActivityAPI)
                recyclerView.adapter = adapter
                // Tắt animation refresh của swipeRefreshLayout
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    // Thêm sự kiện vào Google Calendar
    private fun addReminder(summary: String, startTime: DateTime, endTime: DateTime) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            val event = Event()
                .setSummary(summary)
                .setStart(EventDateTime().setDateTime(startTime))
                .setEnd(EventDateTime().setDateTime(endTime))
            try {
                calendarService.events().insert("primary", event).execute()
                // Chuyển về Main thread để hiển thị Toast và cập nhật giao diện
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderMenuActivityAPI, "Sự kiện đã được thêm thành công!", Toast.LENGTH_SHORT).show()
                    // Gọi hàm tải lại sự kiện sau khi thêm
                    loadEventsForSelectedWeek()
                }
            } catch (e: Exception) {
                Log.e("CalendarError", "Error adding event: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderMenuActivityAPI, "Thêm sự kiện thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Sửa thông tin sự kiện vào Google Calendar
    private fun updateReminder(eventId: String, newSummary: String, newStartDateTime: DateTime, newEndDateTime: DateTime) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            try {
                val event = calendarService.events().get("primary", eventId).execute()
                event.summary = newSummary
                event.start = EventDateTime().setDateTime(newStartDateTime)
                event.end = EventDateTime().setDateTime(newEndDateTime)
                calendarService.events().update("primary", eventId, event).execute()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderMenuActivityAPI, "Sự kiện đã được cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    loadEventsForSelectedWeek()
                }
            } catch (e: Exception) {
                Log.e("CalendarError", "Error updating event: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderMenuActivityAPI, "Cập nhật sự kiện thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Xóa sự kiện khỏi Google Calendar
    private fun deleteReminder(eventId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            try {
                calendarService.events().delete("primary", eventId).execute()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderMenuActivityAPI, "Sự kiện đã được xóa thành công!", Toast.LENGTH_SHORT).show()
                    loadEventsForSelectedWeek()
                }
            } catch (e: Exception) {
                Log.e("CalendarError", "Error deleting event: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderMenuActivityAPI, "Xóa sự kiện thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Menu xác nhận xóa sự kiện
    fun showDeleteConfirmationDialog(eventId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xóa sự kiện")
        builder.setMessage("Bạn có chắc chắn muốn xóa sự kiện này không?")
        builder.setPositiveButton("OK") { dialog, _ ->
            deleteReminder(eventId)
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy bỏ") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    companion object {
        const val REQUEST_CODE_SIGN_IN = 1001
        private const val TAG = "ReminderMenuActivityAPI"
    }
}
