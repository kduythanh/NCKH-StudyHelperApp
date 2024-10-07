package com.example.nlcs.NoteFunction

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nlcs.R
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapter(
    private val context: Context,
    messages: List<Message>
) : RecyclerView.Adapter<MyAdapter.ItemHolder>() {

    // Private copy of the list to prevent external modifications
    private val fullList: MutableList<Message> = messages.toMutableList()
    private var filteredList: MutableList<Message> = fullList.toMutableList()

    // ViewHolder class
    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessTitle: TextView = itemView.findViewById(R.id.tvMessTitle)
        val tvMessContent: TextView = itemView.findViewById(R.id.tvMessContent)
        val ivTrashCan: ImageView = itemView.findViewById(R.id.ivTrashCan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mess_function_item, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = filteredList[position]

        // Set text after trimming unnecessary spaces and newlines
        holder.tvMessTitle.text = item.messTitle.trim()
        holder.tvMessContent.text = item.messContent.trim()

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, position)
        }

        // Handle delete click
        holder.ivTrashCan.setOnClickListener {
            // Show confirmation dialog
            AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú này?")
                .setPositiveButton("Xóa") { dialog, _ ->
                    // Call removeItem when user confirms deletion
                    removeItem(item)
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss() // Dismiss the dialog when user cancels
                }
                .create()
                .show()
        }
    }

    override fun getItemCount(): Int = filteredList.size

    // Expose methods to manipulate the data safely

    // Method to add a new item
    fun addItem(message: Message) {
        fullList.add(0, message) // Add to the beginning of the full list
        filter("") // Refresh filtered list and UI
    }

    // Method to update an existing item
    fun updateItem(updatedMessage: Message) {
        val index = fullList.indexOfFirst { it.messId == updatedMessage.messId }
        if (index != -1) {
            fullList[index] = updatedMessage
            filter("") // Refresh filtered list and UI
        }
    }

    // Method to remove an item
    private fun removeItem(message: Message) {
        val db = FirebaseFirestore.getInstance()

        // Delete from Firestore using the document ID (messId)
        message.messId?.let {
            db.collection("notes")
                .document(it)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Xóa ghi chú thành công!", Toast.LENGTH_SHORT).show()
                    Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                    // Remove from the local lists and update UI
                    fullList.remove(message)
                    filter("") // Refresh filtered list and UI

                    // Notify the activity that an item was deleted
                    onItemDeleted?.invoke(message)
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error deleting document", e)
                    Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // Handle case where messId is null
            Log.w("Firestore", "Attempted to delete a note with a null messId.")
            Toast.makeText(context, "Cannot delete note: invalid ID", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to replace the entire list (e.g., after loading data)
    fun setItems(messages: List<Message>) {
        fullList.clear()
        fullList.addAll(messages)
        filter("") // Refresh filtered list and UI
    }

    // Method to filter messages
    fun filter(query: String) {
        val searchQuery = query.lowercase()
        filteredList = if (searchQuery.isEmpty()) {
            fullList.toMutableList()
        } else {
            fullList.filter { message ->
                message.messTitle.lowercase().contains(searchQuery)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    // Listener for item clicks
    var onItemClick: ((Message, Int) -> Unit)? = null

    // Listener for item deletion
    var onItemDeleted: ((Message) -> Unit)? = null
}
