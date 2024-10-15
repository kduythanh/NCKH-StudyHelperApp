package com.example.nlcs.ui.activities.folder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.adapter.folder.FolderSelectAdapter
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.databinding.ActivityAddToFolderBinding
import com.example.nlcs.ui.activities.create.CreateFolderActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddToFolderActivity : AppCompatActivity() {
//    private val binding by lazy { ActivityAddToFolderBinding.inflate(layoutInflater) }
    private var binding: ActivityAddToFolderBinding? = null
    private val folderDAO by lazy { FolderDAO(this) }
    private lateinit var adapter: FolderSelectAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            usageTracker = UsageTracker(this)
            setContentView(binding?.root)
            setupToolbar()
        CoroutineScope(Dispatchers.Main).launch {
            setupRecyclerView()
            setupCreateNewFolder()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private suspend fun setupRecyclerView() {
        // TODO: get all folder
        firebaseAuth = Firebase.auth
        val userId = firebaseAuth.currentUser?.uid ?: ""
        val folders = folderDAO.getAllFolderByUserId(userId)
        adapter = FolderSelectAdapter(folders, intent.getStringExtra("flashcard_id")!!)
        val linearLayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding?.folderRv?.layoutManager = linearLayoutManager
        binding?.folderRv?.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun setupCreateNewFolder() {
        binding?.createNewFolderTv?.setOnClickListener {
            startActivity(Intent(this, CreateFolderActivity::class.java))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tick, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.done -> {
                onBackPressedDispatcher.onBackPressed()
                Toast.makeText(this, "Đã thêm vào thư mục\n", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            setupRecyclerView()
        }
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
        binding = null
    }
}