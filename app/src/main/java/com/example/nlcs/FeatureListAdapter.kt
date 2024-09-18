package com.example.nlcs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Show list of functions with using time
class FeatureListAdapter(private val features: List<Pair<String, Int>>) : RecyclerView.Adapter<FeatureListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val featureName: TextView = itemView.findViewById(R.id.featureName)
        val featureTime: TextView = itemView.findViewById(R.id.featureTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feature, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, time) = features[position]
        holder.featureName.text = name
        holder.featureTime.text = "$time minutes"
    }

    override fun getItemCount(): Int {
        return features.size
    }
}
