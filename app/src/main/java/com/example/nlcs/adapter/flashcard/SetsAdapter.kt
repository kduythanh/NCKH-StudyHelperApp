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
import com.example.nlcs.databinding.ItemSetBinding
import com.example.nlcs.ui.activities.set.ViewSetActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

class SetsAdapter(
    private val context: Context,
    private val sets: ArrayList<FlashCard>,
    private val isLibrary: Boolean
) : RecyclerView.Adapter<SetsAdapter.SetsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetsViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemSetBinding.inflate(inflater, parent, false)
        return SetsViewHolder(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SetsViewHolder, position: Int) {
        if (isLibrary) {
            //set weight of card
            val params = holder.binding.setCv.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val set = sets[position]
        val cardDAO = CardDAO(context)
        val count: Task<QuerySnapshot> = cardDAO.countCardByFlashCardId(set.id!!)

        holder.binding.setNameTv.text = set.name
        holder.binding.termCountTv.text = "$count terms"
        holder.binding.createdDateTv.text = set.created_at

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
        val binding: ItemSetBinding = ItemSetBinding.bind(itemView)
    }
}
