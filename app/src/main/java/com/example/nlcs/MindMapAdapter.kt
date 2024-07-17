package com.example.nlcs

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore


class MindMapAdapter(
    private var mindMapList: MutableList<MindMap>,
    private val context: Context
): RecyclerView.Adapter<MindMapAdapter.MindMapViewHolder>(){

    // Calling elements of the layout
    class MindMapViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.titleMindMapMenuItemTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteMindMapMenuItemButton)
        val updateButton: ImageView = itemView.findViewById(R.id.EditTitleMindMapMenuItemButton)
    }

    // Setting the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MindMapViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.mind_map_item, parent, false)
        return MindMapViewHolder(itemView)
    }

    // Setting the size of the list
    override fun getItemCount() = mindMapList.size

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: MindMapViewHolder, position: Int) {
        val mindMap= mindMapList[position]

        holder.titleTextView.text = mindMap.title

        // Setting the click listeners for the update button
        holder.updateButton.setOnClickListener{
            showUpdateDialog(mindMap, position)
        }

        // Setting the click listener for the delete button
        holder.deleteButton.setOnClickListener{
            val documentId = mindMap.id
            Log.d(TAG, "Delete button clicked for document with ID: $documentId")

            if (documentId != null) {
                showDeleteConfirmationDialog(documentId, position)
            }
        }

        // Setting the click listener for the item itself
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MindMapActivity::class.java)
            // Passing the mind map item title to the mind map activity
            intent.putExtra("mindMapTitle", mindMap.title)
            context.startActivity(intent)
        }
    }

    private fun showUpdateDialog(mindMap: MindMap, position: Int){
        val oldTitle = mindMap.title

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_mind_map_title, null)
        val builder = AlertDialog.Builder(context).setView(dialogView)
        val editTextTitle = dialogView.findViewById<TextView>(R.id.mindMapEditText)
        editTextTitle.text = oldTitle

        val dialog = builder.create()

        // Setting the click listeners for the the confirm button
        dialogView.findViewById<Button>(R.id.dialogEditButtonConfirm).setOnClickListener {
            val newTitle = editTextTitle.text.toString()
            // Update title in firebase
            updateMindMapTitle(mindMap, newTitle, position)
            // Update the title in the adapter
            notifyItemChanged(position)
            dialog.dismiss()
        }

        // Setting the click listener for the cancel button
        dialogView.findViewById<Button>(R.id.dialogEditButtonCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDeleteConfirmationDialog(documentId: String, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_deletion_mind_map, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        val mindMapIdTextView = dialogView.findViewById<TextView>(R.id.mindMapIdTextView)
        mindMapIdTextView.text = documentId

        dialogView.findViewById<Button>(R.id.dialogConfirmDeletionButtonYes).setOnClickListener {
            deleteMindMapFromFirestore(documentId, position)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.dialogConfirmDeletionButtonNo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Update title method
    private fun updateMindMapTitle(mindMap: MindMap, newTitle: String, position: Int) {
        val documentId = mindMap.id ?: return

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("mindMapTemp").document(documentId)

        docRef.update("title", newTitle)
            .addOnSuccessListener {
                mindMap.title = newTitle
                notifyItemChanged(position)
                Log.d(TAG, "Mind map title updated successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating mind map title", e)
            }
    }

    // Delete mind map method
    private fun deleteMindMapFromFirestore(documentId: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("mindMapTemp").document(documentId)

        docRef.delete()
            .addOnSuccessListener {
                // Safely remove the mind map at the specified position from the local list.
                // Created a mutable copy to avoid potential issues with concurrent modification.
                mindMapList = mindMapList.toMutableList().also {
                    if (position in 0 until it.size) it.removeAt(position)
                }
                // Remove the mind map from the adapter
                notifyItemRemoved(position)
                // Notify the adapter that the list has changed
                notifyItemChanged(position, mindMapList.size)
                Log.d(TAG, "Mind map successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document with ID: $documentId", e)
            }

    }
}


