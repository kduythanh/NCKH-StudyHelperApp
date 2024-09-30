package com.example.nlcs.ui.activities.learn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.adapter.DefineListAdapter
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ActivitySelectDefineBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectDefineActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySelectDefineBinding.inflate(layoutInflater) }
    private val cardDAO by lazy { CardDAO(this) }
    private lateinit var cardList: List<Card> //list of card

    private lateinit var defineListAdapter: DefineListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")

        // Use lifecycleScope to launch the coroutine
        CoroutineScope(Dispatchers.Main).launch {
            // Call the suspend function to get the card list
            val cardDAO = CardDAO(this@SelectDefineActivity)
            val cardList =
                id?.let { cardDAO.getCardsByFlashCardId(it) }   // Fallback to an empty list if id is null

            // Set up the RecyclerView after retrieving the data
            binding.defineRv.layoutManager = LinearLayoutManager(this@SelectDefineActivity, LinearLayoutManager.VERTICAL, false)
            binding.defineRv.setHasFixedSize(true)
            defineListAdapter = cardList?.let { DefineListAdapter(it) }!!
            binding.defineRv.adapter = defineListAdapter
            defineListAdapter.notifyDataSetChanged()
        }
    }

}