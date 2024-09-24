package com.example.nlcs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Hiển thị danh sách các chức năng với thời gian sử dụng
class FeatureListAdapter(private var features: List<Pair<String, Int>>) : RecyclerView.Adapter<FeatureListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val featureName: TextView = itemView.findViewById(R.id.featureName)
        val featureTime: TextView = itemView.findViewById(R.id.featureTime)
        val featureIcon: ImageView = itemView.findViewById(R.id.featureIcon) // Thêm ImageView cho icon
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
        val remainingSecondsAfterHours = timeInSeconds % 3600
        val minutes = remainingSecondsAfterHours / 60
        val seconds = remainingSecondsAfterHours % 60

        holder.featureTime.text = when {
            hours > 0 -> {
                if (minutes > 0 && seconds > 0) {
                    "$hours giờ $minutes phút $seconds giây"
                } else if (minutes > 0) {
                    "$hours giờ $minutes phút"
                } else {
                    "$hours giờ $seconds giây"
                }
            }
            minutes > 0 -> {
                if (seconds > 0) {
                    "$minutes phút $seconds giây"
                } else {
                    "$minutes phút"
                }
            }
            else -> {
                "$seconds giây"
            }
        }

        // Thiết lập icon tương ứng cho tính năng
        holder.featureIcon.setImageResource(getIconResourceByFeatureName(name))
    }

    // Hàm trả về resource ID của icon dựa trên tên tính năng
    private fun getIconResourceByFeatureName(name: String): Int {
        return when (name) {
            "Hẹn giờ tập trung" -> R.drawable.ic_focus // Thay thế bằng tên icon của bạn
            "Ghi chú" -> R.drawable.ic_note // Thay thế bằng tên icon của bạn
            "Sơ đồ tư duy" -> R.drawable.ic_mindmap // Thay thế bằng tên icon của bạn
            "Thẻ ghi nhớ" -> R.drawable.ic_flash_cards // Thay thế bằng tên icon của bạn
            "Nhắc nhở" -> R.drawable.ic_reminder // Thay thế bằng tên icon của bạn
            else -> R.drawable.ic_lock // Thay thế bằng icon mặc định
        }
    }

    // Hàm cập nhật dữ liệu
    fun updateData(newData: List<Pair<String, Int>>) {
        features = newData  // Cập nhật danh sách tính năng
        notifyDataSetChanged()  // Thông báo cho adapter để làm mới giao diện
    }

    override fun getItemCount(): Int {
        return features.size
    }
}
