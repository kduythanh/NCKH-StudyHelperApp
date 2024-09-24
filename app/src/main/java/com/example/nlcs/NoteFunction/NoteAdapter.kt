package com.example.nlcs.NoteFunction

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nlcs.R
import com.google.firebase.firestore.FirebaseFirestore

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

        // Trim unnecessary spaces and newlines
        holder.tvMessTitle.text = item.messTitle.trim()
        holder.tvMessContent.text = item.messContent.trim()

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, position)
        }

        holder.ivTrashCan.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount(): Int {
        return array.size
    }

    private fun removeItem(position: Int) {
        val message = array[position] // Get the message being deleted
        val db = FirebaseFirestore.getInstance()

        // Delete from Firestore using the document ID (messId)
        message.messId?.let {
            db.collection("notes")
                .document(it)
                .delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error deleting document", e)
                }
        }

        // Remove from the local array and update UI
        array.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, array.size)
    }



    var onItemClick: ((Message, Int) -> Unit)? = null
    var onItemClick2: ((Message) -> Unit)? = null
}
