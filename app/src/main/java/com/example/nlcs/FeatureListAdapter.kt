package com.example.nlcs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeatureListAdapter(private var features: List<Pair<String, Int>>) : RecyclerView.Adapter<FeatureListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val featureName: TextView = itemView.findViewById(R.id.featureName)
        val featureTime: TextView = itemView.findViewById(R.id.featureTime)
        val featureIcon: ImageView = itemView.findViewById(R.id.featureIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feature, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, timeInSeconds) = features[position]
        holder.featureName.text = name

        // Tính toán giờ, phút và giây từ thời gian sử dụng tính bằng giây
        val hours = timeInSeconds / 3600
        val minutes = (timeInSeconds % 3600) / 60
        val seconds = timeInSeconds % 60

        holder.featureTime.text = when {
            hours > 0 -> String.format("%d giờ %d phút %d giây", hours, minutes, seconds)
            minutes > 0 -> String.format("%d phút %d giây", minutes, seconds)
            else -> String.format("%d giây", seconds)
        }

        // Thiết lập icon tương ứng cho tính năng
        holder.featureIcon.setImageResource(getIconResourceByFeatureName(name))
    }

    private fun getIconResourceByFeatureName(name: String): Int {
        return when (name) {
            "Hẹn giờ tập trung" -> R.drawable.ic_focus
            "Ghi chú" -> R.drawable.ic_note
            "Sơ đồ tư duy" -> R.drawable.ic_mindmap
            "Thẻ ghi nhớ" -> R.drawable.ic_flash_cards
            "Nhắc nhở" -> R.drawable.ic_reminder
            else -> 0
        }
    }

    // Hàm cập nhật dữ liệu
    fun updateData(newData: List<Pair<String, Int>>) {
        features = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return features.size
    }
}
