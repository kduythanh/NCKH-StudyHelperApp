package com.example.nlcs.ui.activities.learn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.adapter.DefineListAdapter
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ActivitySelectDefineBinding

class SelectDefineActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySelectDefineBinding.inflate(layoutInflater) }
    private val cardDAO by lazy { CardDAO(this) }
    private lateinit var cardList: List<Card> //list of card

    private lateinit var defineListAdapter: DefineListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val id = intent.getStringExtra("id")
        cardList = cardDAO.getCardsByFlashCardId(id)
        binding.defineRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.defineRv.setHasFixedSize(true)
        defineListAdapter = DefineListAdapter(cardList)
        binding.defineRv.adapter = defineListAdapter
        defineListAdapter.notifyDataSetChanged()


    }

}