package com.example.nlcs

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.model.EventAttendee
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
    private lateinit var noEventsTextView: TextView
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var currentWeekStartDate: JavaCalendar = JavaCalendar.getInstance()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_menu_api)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // Cấu hình Google Sign-In
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Khởi tạo signInLauncher
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
        noEventsTextView = findViewById(R.id.noEventsTextView)
        exitButton = findViewById(R.id.exitButton) // Xử lý nút thoát khỏi chức năng
        exitButton.setOnClickListener { finish() }
        recyclerView = findViewById(R.id.recyclerView) // Cấu hình RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout) // Cấu hình SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener { loadEventsForSelectedWeek() }
        previousWeekButton = findViewById(R.id.previousWeekButton) // Button chuyển về tuần trước
        previousWeekButton.setOnClickListener {
            currentWeekStartDate.add(JavaCalendar.DATE, -7) // Trừ 7 ngày để chuyển về tuần trước
            loadEventsForSelectedWeek()
        }
        nextWeekButton = findViewById(R.id.nextWeekButton) // Button chuyển về tuần sau
        nextWeekButton.setOnClickListener {
            currentWeekStartDate.add(JavaCalendar.DATE, 7) // Cộng 7 ngày để chuyển về tuần sau
            loadEventsForSelectedWeek()
        }
        addButton = findViewById(R.id.reminderAddButton) // Button thêm nhắc nhở
        addButton.setOnClickListener { showAddEventDialog() }
        signOut() // Thoát khỏi Google Sign-In, đăng nhập lại
    }
    // Load danh sách các sự kiện trong tuần được chọn
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadEventsForSelectedWeek() {
        val startOfWeek = currentWeekStartDate.clone() as JavaCalendar
        startOfWeek.set(JavaCalendar.DAY_OF_WEEK, JavaCalendar.MONDAY) // 0h ngày thứ 2 của tuần đang xét
        startOfWeek.set(JavaCalendar.HOUR_OF_DAY, 0)
        startOfWeek.set(JavaCalendar.MINUTE, 0)
        startOfWeek.set(JavaCalendar.SECOND, 0)
        startOfWeek.set(JavaCalendar.MILLISECOND, 0)
        val endOfWeek = startOfWeek.clone() as JavaCalendar
        endOfWeek.add(JavaCalendar.WEEK_OF_YEAR, 1) // 0h ngày thứ 2 của tuần kế tiếp
        fetchCalendarEvents(startOfWeek.time, endOfWeek.time) // Gọi hàm fetchCalendarEvents với ngày bắt đầu và kết thúc
    }
    // Hiển thị menu thêm sự kiện
    @RequiresApi(Build.VERSION_CODES.O)
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

                // Lấy dữ liệu từ các EditText cho ngày bắt đầu
                val startDay = dialog.findViewById<EditText>(R.id.startDayInput)?.text.toString()
                val startMonth = dialog.findViewById<EditText>(R.id.startMonthInput)?.text.toString()
                val startYear = dialog.findViewById<EditText>(R.id.startYearInput)?.text.toString()
                val startHour = dialog.findViewById<EditText>(R.id.startHourInput)?.text.toString()
                val startMinute = dialog.findViewById<EditText>(R.id.startMinuteInput)?.text.toString()

                // Lấy dữ liệu từ các EditText cho ngày kết thúc
                val endDay = dialog.findViewById<EditText>(R.id.endDayInput)?.text.toString()
                val endMonth = dialog.findViewById<EditText>(R.id.endMonthInput)?.text.toString()
                val endYear = dialog.findViewById<EditText>(R.id.endYearInput)?.text.toString()
                val endHour = dialog.findViewById<EditText>(R.id.endHourInput)?.text.toString()
                val endMinute = dialog.findViewById<EditText>(R.id.endMinuteInput)?.text.toString()

                val participantsEmails = dialog.findViewById<EditText>(R.id.participantsEmails)?.text.toString()

                // Xác thực đầu vào
                if (validateEventInput(eventTitle, startDay, startMonth, startYear, startHour, startMinute, endDay, endMonth, endYear, endHour, endMinute, participantsEmails)) {
                    // Chuyển đổi ngày giờ thành định dạng cần thiết
                    val startDateTime = parseDateTime(startDay, startMonth, startYear, startHour, startMinute)
                    val endDateTime = parseDateTime(endDay, endMonth, endYear, endHour, endMinute)
                    val emailList = parseEmails(participantsEmails) // Hàm tách email thành danh sách

                    addReminder(eventTitle, startDateTime, endDateTime, emailList)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sự kiện và email hợp lệ!", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton?.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    // Hàm phân tách địa chỉ email
    private fun parseEmails(emails: String): List<String> {
        return emails.split(",")           // Tách các email theo dấu phẩy
            .map { it.trim() }             // Loại bỏ khoảng trắng ở đầu và cuối mỗi email
            .filter { it.isNotEmpty() }    // Lọc bỏ những email rỗng (trường hợp người dùng nhập thừa dấu phẩy)
    }
    // Hiển thị menu chỉnh sửa thông tin sự kiện
    @RequiresApi(Build.VERSION_CODES.O)
    fun showEditEventDialog(eventId: String, currentTitle: String, currentStartDate: DateTime, currentEndDate: DateTime, participantsEmails: String) {
        val dialogView = layoutInflater.inflate(R.layout.edit_event_layout, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        dialog.setOnShowListener {
            val titleEditText = dialog.findViewById<EditText>(R.id.eventTitleEditText)
            val startDayEditText = dialog.findViewById<EditText>(R.id.startDayInputEditText)
            val startMonthEditText = dialog.findViewById<EditText>(R.id.startMonthInputEditText)
            val startYearEditText = dialog.findViewById<EditText>(R.id.startYearInputEditText)
            val startHourEditText = dialog.findViewById<EditText>(R.id.startHourInputEditText)
            val startMinuteEditText = dialog.findViewById<EditText>(R.id.startMinuteInputEditText)

            val endDayEditText = dialog.findViewById<EditText>(R.id.endDayInputEditText)
            val endMonthEditText = dialog.findViewById<EditText>(R.id.endMonthInputEditText)
            val endYearEditText = dialog.findViewById<EditText>(R.id.endYearInputEditText)
            val endHourEditText = dialog.findViewById<EditText>(R.id.endHourInputEditText)
            val endMinuteEditText = dialog.findViewById<EditText>(R.id.endMinuteInputEditText)

            val participantsEmailsEditText = dialog.findViewById<EditText>(R.id.participantsEmailsEditText)
            val saveButton = dialog.findViewById<Button>(R.id.saveEditedEventButton)
            val cancelButton = dialog.findViewById<Button>(R.id.cancelEditedEventButton)

            // Thiết lập dữ liệu hiện tại cho các trường
            titleEditText?.setText(currentTitle)

            // Lấy thông tin từ currentStartDate
            val startCalendar = JavaCalendar.getInstance()
            startCalendar.timeInMillis = currentStartDate.value

            startDayEditText?.setText(startCalendar.get(JavaCalendar.DAY_OF_MONTH).toString())
            startMonthEditText?.setText((startCalendar.get(JavaCalendar.MONTH) + 1).toString()) // Tháng bắt đầu từ 0
            startYearEditText?.setText(startCalendar.get(JavaCalendar.YEAR).toString())
            startHourEditText?.setText(startCalendar.get(JavaCalendar.HOUR_OF_DAY).toString())
            startMinuteEditText?.setText(startCalendar.get(JavaCalendar.MINUTE).toString())

            // Lấy thông tin từ currentEndDate
            val endCalendar = JavaCalendar.getInstance()
            endCalendar.timeInMillis = currentEndDate.value

            endDayEditText?.setText(endCalendar.get(JavaCalendar.DAY_OF_MONTH).toString())
            endMonthEditText?.setText((endCalendar.get(JavaCalendar.MONTH) + 1).toString())
            endYearEditText?.setText(endCalendar.get(JavaCalendar.YEAR).toString())
            endHourEditText?.setText(endCalendar.get(JavaCalendar.HOUR_OF_DAY).toString())
            endMinuteEditText?.setText(endCalendar.get(JavaCalendar.MINUTE).toString())

            participantsEmailsEditText?.setText(participantsEmails)

            saveButton?.setOnClickListener {
                val newTitle = titleEditText?.text.toString()
                val newStartDay = startDayEditText?.text.toString()
                val newStartMonth = startMonthEditText?.text.toString()
                val newStartYear = startYearEditText?.text.toString()
                val newStartHour = startHourEditText?.text.toString()
                val newStartMinute = startMinuteEditText?.text.toString()

                val newEndDay = endDayEditText?.text.toString()
                val newEndMonth = endMonthEditText?.text.toString()
                val newEndYear = endYearEditText?.text.toString()
                val newEndHour = endHourEditText?.text.toString()
                val newEndMinute = endMinuteEditText?.text.toString()

                val newParticipantsEmails = participantsEmailsEditText?.text.toString()

                if (validateEventInput(newTitle, newStartDay, newStartMonth, newStartYear, newStartHour, newStartMinute, newEndDay, newEndMonth, newEndYear, newEndHour, newEndMinute, newParticipantsEmails)) {
                    val newStartDateTime = parseDateTime(newStartDay, newStartMonth, newStartYear, newStartHour, newStartMinute)
                    val newEndDateTime = parseDateTime(newEndDay, newEndMonth, newEndYear, newEndHour, newEndMinute)
                    val newEmailList = parseEmails(newParticipantsEmails)
                    updateReminder(eventId, newTitle, newStartDateTime, newEndDateTime, newEmailList)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sự kiện và email hợp lệ!", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton?.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }
    // Kiểm tra sự kiện nhập vào đã hợp lệ chưa
    private fun validateEventInput(
        title: String, startDay: String, startMonth: String, startYear: String, startHour: String, startMinute: String,
        endDay: String, endMonth: String, endYear: String, endHour: String, endMinute: String, participantsEmails: String
    ): Boolean {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS

        // Kiểm tra tính hợp lệ của các phần ngày tháng giờ
        val isStartDateValid = isValidDate(startDay, startMonth, startYear, startHour, startMinute)
        val isEndDateValid = isValidDate(endDay, endMonth, endYear, endHour, endMinute)

        // Kiểm tra xem ngày bắt đầu có trước ngày kết thúc không
        val isDateRangeValid = isStartDateValid && isEndDateValid && compareDates(startDay, startMonth, startYear, startHour, startMinute,
            endDay, endMonth, endYear, endHour, endMinute)

        // Kiểm tra tính hợp lệ của email
        val emailsValid = if (participantsEmails.isNotEmpty()) {
            participantsEmails.split(",").all { emailPattern.matcher(it.trim()).matches() }
        } else { true }

        return title.isNotEmpty() && isDateRangeValid && emailsValid
    }
    // Kiểm tra ngày nhập vào đã hợp lệ chưa
    private fun isValidDate(day: String, month: String, year: String, hour: String, minute: String): Boolean {
        val dayInt = day.toIntOrNull() ?: return false
        val monthInt = month.toIntOrNull() ?: return false
        val yearInt = year.toIntOrNull() ?: return false
        val hourInt = hour.toIntOrNull() ?: return false
        val minuteInt = minute.toIntOrNull() ?: return false

        // Kiểm tra tháng hợp lệ
        if (monthInt < 1 || monthInt > 12) return false

        // Kiểm tra ngày hợp lệ
        val calendar = JavaCalendar.getInstance()
        calendar.set(yearInt, monthInt - 1, dayInt)

        // Kiểm tra ngày trong tháng
        if (dayInt != calendar.get(JavaCalendar.DAY_OF_MONTH)) return false

        // Kiểm tra giờ hợp lệ
        if (hourInt < 0 || hourInt > 23) return false

        // Kiểm tra phút hợp lệ
        if (minuteInt < 0 || minuteInt > 59) return false

        return true
    }
    private fun compareDates(startDay: String, startMonth: String, startYear: String, startHour: String, startMinute: String,
                             endDay: String, endMonth: String, endYear: String, endHour: String, endMinute: String): Boolean {
        // Chuyển đổi sang số
        val startCalendar = JavaCalendar.getInstance()
        startCalendar.set(startYear.toInt(), startMonth.toInt() - 1, startDay.toInt(), startHour.toInt(), startMinute.toInt())

        val endCalendar = JavaCalendar.getInstance()
        endCalendar.set(endYear.toInt(), endMonth.toInt() - 1, endDay.toInt(), endHour.toInt(), endMinute.toInt())

        return startCalendar.timeInMillis <= endCalendar.timeInMillis
    }
    // Chuyển đổi định dạng ngày tháng năm theo quy định
    private fun parseDateTime(day: String, month: String, year: String, hour: String, minute: String): DateTime {
        // Đảm bảo các phần nhập vào được định dạng chính xác
        val formattedDate = String.format("%02d/%02d/%04d %02d:%02d", day.toInt(), month.toInt(), year.toInt(), hour.toInt(), minute.toInt())

        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val dateTime = inputFormat.parse(formattedDate) ?: throw IllegalArgumentException("Invalid date/time format")
        return DateTime(outputFormat.format(dateTime))
    }
    // Sign in
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
    // Sign out
    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            signIn() // Đăng xuất thành công, bắt đầu quá trình đăng nhập lại
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchCalendarEvents(startDate: Date, endDate: Date) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = JavaCalendar.getInstance().apply {
                time = endDate
                add(JavaCalendar.DAY_OF_MONTH, -1) // Lùi lại 1 ngày
            }
            val adjustedEndDate = calendar.time
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            // Chuyển đổi startDate và endDate thành định dạng DateTime (ISO 8601)
            val timeMin = DateTime(startDate)
            val timeMax = DateTime(endDate)

            Log.d("CalendarEvents", "Fetching events from: $timeMin to $timeMax")
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
            // Gửi thông báo cho sự kiện sắp diễn ra
            val currentTime = System.currentTimeMillis()
            val thirtyMinutesLater = currentTime + 30 * 60 * 1000 // 30 phút sau

            events.forEach { event ->
                val start = event.start.dateTime?.value ?: event.start.date.value
                if (start in currentTime..thirtyMinutesLater) {
                    // Gửi thông báo
                    NotificationService(this@ReminderMenuActivityAPI).sendNotification(event.summary, event.start)
                }
            }
            Log.d("CalendarEvents", "Fetched ${events.size} events")
            withContext(Dispatchers.Main) { // Cập nhật giao diện trong luồng chính (Main thread)
                // Cập nhật dữ liệu cho RecyclerView adapter
                val adapter = EventsAdapter(events, this@ReminderMenuActivityAPI)
                recyclerView.adapter = adapter
                swipeRefreshLayout.isRefreshing = false // Tắt animation refresh của swipeRefreshLayout
                // Kiếm tra nếu không có sự kiện nào
                if (events.isEmpty()) {
                    noEventsTextView.text = "Không tìm thấy sự kiện nào từ ngày\n${startDate.toFormattedString()} đến ngày ${adjustedEndDate.toFormattedString()}"
                } else {
                    noEventsTextView.text = "Các sự kiện diễn ra từ ngày\n${startDate.toFormattedString()} đến ngày ${adjustedEndDate.toFormattedString()}"
                }
            }
        }
    }
    // Hàm chuyển đổi Date thành chuỗi định dạng mong muốn
    private fun Date.toFormattedString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(this)
    }
    // Thêm sự kiện vào Google Calendar
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addReminder(summary: String, startTime: DateTime, endTime: DateTime, attendeesEmails: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            val event = Event().apply {
                setSummary(summary)
                setStart(EventDateTime().setDateTime(startTime))
                setEnd(EventDateTime().setDateTime(endTime))
                // Thêm danh sách người tham gia
                attendees = attendeesEmails.map { email ->
                    EventAttendee().setEmail(email)
                }
            }
            try {
                val createdEvent = calendarService.events().insert("primary", event).execute()
                // Sau khi sự kiện được thêm thành công, lên lịch thông báo
                if (createdEvent != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReminderMenuActivityAPI, "Sự kiện đã được thêm thành công!", Toast.LENGTH_SHORT).show()
                        loadEventsForSelectedWeek()
                    }
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateReminder(eventId: String, newSummary: String, newStartDateTime: DateTime, newEndDateTime: DateTime, newAttendeesEmails: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory()
            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Reminder App")
                .build()
            try {
                val event = calendarService.events().get("primary", eventId).execute()
                event.summary = newSummary
                event.start = EventDateTime().setDateTime(newStartDateTime)
                event.end = EventDateTime().setDateTime(newEndDateTime)
                val attendees = newAttendeesEmails.map { email ->
                    EventAttendee().setEmail(email)
                }
                event.attendees = attendees
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteReminder(eventId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory()
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
    @RequiresApi(Build.VERSION_CODES.O)
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