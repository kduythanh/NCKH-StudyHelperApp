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

interface MindMapListener {
    fun onMindMapDeleted()
}

class MindMapAdapter(
    private var mindMapList: MutableList<MindMap>,
    private val context: Context,
    private val listener: MindMapListener
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
            val mindMapID = mindMap.mindMapID
            val mindMapTitle = mindMap.title
            if (documentId != null) {
                showDeleteConfirmationDialog(documentId, position, mindMapID, mindMapTitle)
            }
        }

        // Set the click listener for the item itself to go to the mind map activity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MindMapActivity::class.java)
            // Passing the mind map item title to the mind map activity
            intent.putExtra("mindMapTitle", mindMap.title)
            intent.putExtra("documentID", mindMap.id)
            intent.putExtra("mindMapID", mindMap.mindMapID)
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

    private fun showDeleteConfirmationDialog(documentId: String, position: Int, mindMapID: String?, mindMapTitle: String?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_deletion_mind_map, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        val mindMapIdTextView = dialogView.findViewById<TextView>(R.id.mindMapTitleTextView)
        mindMapIdTextView.text = mindMapTitle

        dialogView.findViewById<Button>(R.id.dialogConfirmDeletionButtonYes).setOnClickListener {
            deleteMindMapFromFirestore(documentId, position, mindMapID)
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
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating mind map title", e)
            }
    }

    private fun deleteMindMapFromFirestore(documentId: String, position: Int, mindMapID: String?) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("mindMapTemp").document(documentId)
        docRef.delete()
            .addOnSuccessListener {
                // Check if the position is valid before removing the item
                if (position in 0 until mindMapList.size ) {
                    // Remove the mind map from the local list directly
                    mindMapList.removeAt(position)

                    // Notify the adapter that the item was removed
                    notifyItemRemoved(position)

                    // Log the new size after deletion

                    // Notify that the list has changed
                    notifyItemRangeChanged(position, itemCount - position)

                    // Notify the listener to refresh the mind maps
                    listener.onMindMapDeleted()

                } else {
                    Log.w(TAG, "Attempted to delete at an invalid position: $position")
                }

                // Delete the mind map from Neo4j
                val neo4jService = Neo4jService("bolt+s://25190e75.databases.neo4j.io", "neo4j", "j_j-RCzouI3et4G2bvF0RTW83eXCws-aoQJstrQgUts")
                neo4jService.deleteAllNodes(mindMapID)
                neo4jService.close()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document with ID: $documentId", e)
            }
    }

}


