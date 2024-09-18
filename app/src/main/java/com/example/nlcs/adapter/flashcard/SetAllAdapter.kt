package com.example.nlcs.adapter.flashcard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ItemSetAllBinding
import com.example.nlcs.ui.activities.set.ViewSetActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

class SetAllAdapter(private val context: Context, private val sets: List<FlashCard>) :
    RecyclerView.Adapter<SetAllAdapter.SetsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetsViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemSetAllBinding.inflate(inflater, parent, false)
        return SetsViewHolder(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SetsViewHolder, position: Int) {
        val set = sets[position]
        val cardDAO = CardDAO(context)
        val count: Task<QuerySnapshot> = cardDAO.countCardByFlashCardId(set.id!!)

        holder.binding.setNameTv.text = set.name
        holder.binding.termCountTv.text = "$count terms"

        holder.itemView.setOnClickListener { v: View? ->
            val intent = Intent(context, ViewSetActivity::class.java)
            intent.putExtra("id", set.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return sets.size
    }

    class SetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemSetAllBinding = ItemSetAllBinding.bind(itemView)
    }
}
