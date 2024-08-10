package com.example.nlcs.NoteFunction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R

class MyAdapter(var context: Context, var array: ArrayList<Message>) : RecyclerView.Adapter<MyAdapter.ItemHolder>() {

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvMessTitle: TextView = itemView.findViewById(R.id.tvMessTitle)
        var tvMessContent: TextView = itemView.findViewById(R.id.tvMessContent)
        var ivTrashCan: ImageView = itemView.findViewById(R.id.ivTrashCan)  // Add this line
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mess_function_item, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = array[position]
        holder.tvMessTitle.text = item.messTitle
        holder.tvMessContent.text = item.messContent

        // Handle item click if needed
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, position)
        }

        // Set up the click listener for the trash can
        holder.ivTrashCan.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount(): Int {
        return array.size
    }

    private fun removeItem(position: Int) {
        array.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, array.size)
    }

    var onItemClick: ((Message, Int) -> Unit)? = null
    var onItemClick2: ((Message) -> Unit)? = null
}
