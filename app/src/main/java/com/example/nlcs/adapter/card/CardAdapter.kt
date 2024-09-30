package com.example.nlcs.adapter.card

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.adapter.card.CardAdapter.CardViewHolder
import com.example.nlcs.data.model.Card
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ItemCardAddBinding

class CardAdapter(private val context: Context, private val cards: ArrayList<Card>) :
    RecyclerView.Adapter<CardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemCardAddBinding.inflate(inflater, parent, false)
        return CardViewHolder(binding.root)
    }

    override fun onBindViewHolder(
        holder: CardViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val card: Card = cards[position]
        //val card = cards[position]
        holder.removeTextWatchers()

        if (position > 1) {
            holder.binding.termEt.requestFocus()
        }

        holder.binding.termEt.setText(card.GetFront())
        holder.binding.definitionEt.setText(card.GetBack())


        val frontWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                card.SetFront(s.toString().trim { it <= ' ' })
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                card.SetFront(s.toString().trim { it <= ' ' })
            }

            override fun afterTextChanged(s: Editable) {
                card.SetFront(s.toString().trim { it <= ' ' })
            }
        }

        val backWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                card.SetBack(s.toString().trim { it <= ' ' })
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                card.SetBack(s.toString().trim { it <= ' ' })
            }

            override fun afterTextChanged(s: Editable) {
                card.SetBack(s.toString().trim { it <= ' ' })
            }
        }

        holder.setTextWatchers(frontWatcher, backWatcher)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemCardAddBinding = ItemCardAddBinding.bind(itemView)
        private var frontWatcher: TextWatcher? = null
        private var backWatcher: TextWatcher? = null

        fun removeTextWatchers() {
            if (frontWatcher != null) {
                binding.termEt.removeTextChangedListener(frontWatcher)
            }
            if (backWatcher != null) {
                binding.definitionEt.removeTextChangedListener(backWatcher)
            }
        }

        fun setTextWatchers(frontWatcher: TextWatcher?, backWatcher: TextWatcher?) {
            this.frontWatcher = frontWatcher
            this.backWatcher = backWatcher
            binding.termEt.addTextChangedListener(frontWatcher)
            binding.definitionEt.addTextChangedListener(backWatcher)
        }
    }
}
