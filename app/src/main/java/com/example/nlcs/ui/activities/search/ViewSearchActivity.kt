package com.example.nlcs.ui.activities.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.adapter.flashcard.SetAllAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ActivityViewSearchBinding
import java.util.Locale

class ViewSearchActivity : AppCompatActivity() {
    private var binding: ActivityViewSearchBinding? = null
    private var flashCards: ArrayList<FlashCard>? = null
    private var flashCardDAO: FlashCardDAO? = null
    private var setAllAdapter: SetAllAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private suspend fun setupSets() {
        val flashCards = flashCardDAO?.getAllFlashCardPublic() ?: emptyList()

        // Initialize adapter and set it to RecyclerView
        setAllAdapter = SetAllAdapter(this, flashCards)
        binding?.setsRv?.apply {
            layoutManager = LinearLayoutManager(this@ViewSearchActivity)
            adapter = setAllAdapter
        }

        // Show or hide the container based on flashCards content
        binding?.setsCl?.visibility = if (flashCards.isEmpty()) View.GONE else View.VISIBLE
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
            if (flashCard.name!!.lowercase(Locale.getDefault())
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
}