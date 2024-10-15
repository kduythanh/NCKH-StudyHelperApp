package com.example.nlcs.ui.activities.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.UsageTracker
import com.example.nlcs.adapter.flashcard.SetAllAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ActivityViewSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class ViewSearchActivity : AppCompatActivity() {
    private var binding: ActivityViewSearchBinding? = null
    private var flashCards: ArrayList<FlashCard>? = null
    private var flashCardDAO: FlashCardDAO? = null
    private var setAllAdapter: SetAllAdapter? = null

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageTracker = UsageTracker(this)
        // Initialize View Binding synchronously
        binding = ActivityViewSearchBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setupBackButton()
        setupData()
        setupSets()
        setupSearchView()

    }

    private fun setupBackButton() {
        binding!!.backIv.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()  // Directly call the onBackPressedDispatcher
        }
    }

    private fun setupData() {
        flashCardDAO = FlashCardDAO(this)
    }

    private fun setupSets() {
        flashCards = flashCardDAO?.getAllFlashCardPublic()
        setAllAdapter = flashCards?.let { SetAllAdapter(this, it) }

        // Set up RecyclerView
        binding?.setsRv?.layoutManager = LinearLayoutManager(this)
        binding?.setsRv?.adapter = setAllAdapter

        // Set visibility based on flashCards list
        binding?.setsCl?.visibility   = if (flashCards?.isEmpty() == true) View.GONE else View.VISIBLE

    }


    private fun setupSearchView() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                handleSearchQuery(newText)
                return true
            }
        })
    }

    private fun handleSearchQuery(newText: String) {
        val filteredFlashCards = ArrayList<FlashCard>()
        for (flashCard in flashCards!!) {
            if (flashCard.GetName()!!.lowercase(Locale.getDefault())
                    .contains(newText.lowercase(Locale.getDefault()))
            ) {
                filteredFlashCards.add(flashCard)
            }
        }
        updateAdapters(filteredFlashCards)
        updateVisibility(newText, filteredFlashCards)
    }

    private fun updateAdapters(flashCards: ArrayList<FlashCard>) {
        setAllAdapter = SetAllAdapter(this, flashCards)
        binding!!.setsRv.adapter = setAllAdapter
    }

    private fun updateVisibility(newText: String, flashCards: ArrayList<FlashCard>) {
        val isSearchEmpty = newText.isEmpty()
        val isFlashCardsEmpty = flashCards.isEmpty()

        binding!!.setsCl.visibility =
            if (isSearchEmpty || isFlashCardsEmpty) View.GONE else View.VISIBLE
        binding!!.enterTopicTv.visibility = if (isSearchEmpty) View.VISIBLE else View.GONE
        binding!!.noResultTv.visibility =
            if (isSearchEmpty || !isFlashCardsEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
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