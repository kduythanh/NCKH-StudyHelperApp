package com.example.nlcs.adapter.card

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.adapter.card.ViewSetAdapter.ViewSetViewHolder
import com.example.nlcs.data.model.Card
import com.example.nlcs.databinding.ItemViewSetBinding

class ViewSetAdapter(private val context: Context, private val cards: ArrayList<Card>) :
    RecyclerView.Adapter<ViewSetViewHolder>() {
    private var textToSpeech: TextToSpeech? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewSetViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemViewSetBinding.inflate(inflater, parent, false)
        return ViewSetViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewSetViewHolder, position: Int) {
        val card: Card = cards[position]
        holder.binding.backTv.setText(card.GetFront())
        holder.binding.frontTv.setText(card.GetBack())
        holder.binding.cardViewFlip.flipDuration = 450
        holder.binding.cardViewFlip.isFlipEnabled = true
        holder.binding.cardViewFlip.setOnClickListener { v: View? ->
            holder.binding.cardViewFlip.flipTheView()
            if (textToSpeech != null) {
                textToSpeech!!.stop()
                textToSpeech!!.shutdown()
            }
        }
        holder.binding.soundIv.setOnClickListener { v: View? ->
            if (!holder.binding.backTv.text.toString().isEmpty()) {
                textToSpeech = TextToSpeech(context) { status: Int ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = textToSpeech!!.setLanguage(textToSpeech!!.voice.locale)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val params = Bundle()
                            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                            textToSpeech!!.speak(
                                holder.binding.backTv.text.toString(),
                                TextToSpeech.QUEUE_FLUSH,
                                params,
                                "UniqueID"
                            )
                        }
                    } else {
                        Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    class ViewSetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemViewSetBinding = ItemViewSetBinding.bind(itemView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
    }
}