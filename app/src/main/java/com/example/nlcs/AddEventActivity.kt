//package com.example.nlcs
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.nlcs.ReminderMenuActivityAPI.Companion.REQUEST_CODE_SIGN_IN
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.tasks.Task
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
//import com.google.api.client.json.jackson2.JacksonFactory
//import com.google.api.client.util.DateTime
//import com.google.api.services.calendar.model.Event
//import com.google.api.services.calendar.model.EventDateTime
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.Calendar as JavaCalendarService
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.services.calendar.Calendar as GoogleCalendarService
//import com.google.api.services.calendar.CalendarScopes
//
//class AddEventActivity : AppCompatActivity() {
//
//    private lateinit var titleInput: EditText
//    private lateinit var startDateButton: Button
//    private lateinit var startTimeButton: Button
//    private lateinit var endDateButton: Button
//    private lateinit var endTimeButton: Button
//    private lateinit var btnSaveReminder: Button
//    private lateinit var credential: GoogleAccountCredential
//
//    private var startDate: String? = null
//    private var startTime: String? = null
//    private var endDate: String? = null
//    private var endTime: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_reminder)
//
//        titleInput = findViewById(R.id.titleInput)
//        startDateButton = findViewById(R.id.startDateButton)
//        startTimeButton = findViewById(R.id.startTimeButton)
//        endDateButton = findViewById(R.id.endDateButton)
//        endTimeButton = findViewById(R.id.endTimeButton)
//        btnSaveReminder = findViewById(R.id.btnSaveReminder)
//
//        startDateButton.setOnClickListener {
//            showDatePicker { year, month, day ->
//                startDate = "$year-${month + 1}-$day"
//                startDateButton.text = startDate
//            }
//        }
//
//        startTimeButton.setOnClickListener {
//            showTimePicker { hour, minute ->
//                startTime = "$hour:$minute"
//                startTimeButton.text = startTime
//            }
//        }
//
//        endDateButton.setOnClickListener {
//            showDatePicker { year, month, day ->
//                endDate = "$year-${month + 1}-$day"
//                endDateButton.text = endDate
//            }
//        }
//
//        endTimeButton.setOnClickListener {
//            showTimePicker { hour, minute ->
//                endTime = "$hour:$minute"
//                endTimeButton.text = endTime
//            }
//        }
//
//        btnSaveReminder.setOnClickListener {
//            val title = titleInput.text.toString()
//            if (title.isNotEmpty() && startDate != null && startTime != null && endDate != null && endTime != null) {
//                val startDateTime = "$startDate" + "T" + "$startTime:00Z"
//                val endDateTime = "$endDate" + "T" + "$endTime:00Z"
//                addEventToCalendar(title, startDateTime, endDateTime)
//            } else {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun showDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
//        val calendar = JavaCalendarService.getInstance()
//        DatePickerDialog(
//            this,
//            { _, year, month, dayOfMonth -> onDateSet(year, month, dayOfMonth) },
//            calendar.get(JavaCalendarService.YEAR),
//            calendar.get(JavaCalendarService.MONTH),
//            calendar.get(JavaCalendarService.DAY_OF_MONTH)
//        ).show()
//    }
//
//    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
//        val calendar = JavaCalendarService.getInstance()
//        TimePickerDialog(
//            this,
//            { _, hourOfDay, minute -> onTimeSet(hourOfDay, minute) },
//            calendar.get(JavaCalendarService.HOUR_OF_DAY),
//            calendar.get(JavaCalendarService.MINUTE),
//            true
//        ).show()
//    }
//    private fun initGoogleAccountCredential(account: GoogleSignInAccount) {
//        credential = GoogleAccountCredential.usingOAuth2(
//            this, listOf(CalendarScopes.CALENDAR)
//        ).setSelectedAccount(account.account)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }
//
//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            val account = completedTask.getResult(ApiException::class.java)
//            if (account != null) {
//                initGoogleAccountCredential(account)
//                // Gọi các phương thức cần thiết sau khi khởi tạo credential
//            }
//        } catch (e: ApiException) {
//            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
//        }
//    }
//
//    private fun addEventToCalendar(title: String, startDateTime: String, endDateTime: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val transport = GoogleNetHttpTransport.newTrustedTransport()
//            val jsonFactory = JacksonFactory.getDefaultInstance()
//
//            val calendarService = GoogleCalendarService.Builder(transport, jsonFactory, credential)
//                .setApplicationName("Reminder App")
//                .build()
//
//            val event = Event()
//                .setSummary(title)
//                .setStart(EventDateTime().setDateTime(DateTime(startDateTime)))
//                .setEnd(EventDateTime().setDateTime(DateTime(endDateTime)))
//
//            try {
//                calendarService.events().insert("primary", event).execute()
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@AddEventActivity, "Event added successfully", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@AddEventActivity, "Error adding event: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    companion object {
//        const val REQUEST_CODE_SIGN_IN = 1001
//        private const val TAG = "ReminderMenuActivityAPI"
//    }
//}
