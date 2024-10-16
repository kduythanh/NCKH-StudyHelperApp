package com.example.nlcs.ui.activities.set

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.adapter.flashcard.SetFolderViewAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ActivityAddFlashCardBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFlashCardActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityAddFlashCardBinding.inflate(layoutInflater)
    }

//    private var binding: ActivityAddFlashCardBinding? = null
    private lateinit var flashCardDAO: FlashCardDAO
    private lateinit var flashCardList: ArrayList<FlashCard>
    private lateinit var adapter: SetFolderViewAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    //private lateinit var userSharePreferences: UserSharePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
        usageTracker = UsageTracker(this)
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding?.toolbar)
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        // Start a coroutine to fetch flashcards asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            // Get the current user ID
            firebaseAuth = Firebase.auth
            val userId = firebaseAuth.currentUser?.uid ?: ""

            // Initialize the DAO
            flashCardDAO = FlashCardDAO(this@AddFlashCardActivity) // Replace with actual activity or context

            // Fetch the flashcards asynchronously
            val flashCardList = flashCardDAO.getAllFlashCardByUserId(userId)

            // Set up the RecyclerView with the fetched flashcards
            adapter = SetFolderViewAdapter(flashCardList, true, intent.getStringExtra("id_folder")!!)
            val linearLayoutManager = LinearLayoutManager(this@AddFlashCardActivity, LinearLayoutManager.VERTICAL, false)
            binding?.flashcardRv?.layoutManager = linearLayoutManager
            binding?.flashcardRv?.adapter = adapter

            // Notify adapter that data has changed
            adapter.notifyDataSetChanged()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tick, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.done -> {
                onBackPressedDispatcher.onBackPressed()
             //   Toast.makeText(this, "Đã thêm vào thư mục\n", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        // Lưu thời gian bắt đầu (mốc thời gian hiện tại) để tính thời gian sử dụng khi Activity bị tạm dừng
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()

        // Tính toán thời gian sử dụng Sơ đồ tư duy
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Thẻ ghi nhớ", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Thẻ ghi nhớ", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Đặt binding thành null an toàn khi Activity bị hủy
//        binding = null
    }


}