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
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


class ReminderAdapter(
    private var reminderList: MutableList<Reminder>,
    private val context: Context
): RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>(){

    // Calling elements of the layout
    class ReminderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.titleReminderMenuItemTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteReminderMenuItemButton)
        val updateButton: ImageView = itemView.findViewById(R.id.EditReminderMenuItemButton)
    }

    // Setting the layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.mind_map_item, parent, false)
        return ReminderViewHolder(itemView)
    }

    // Setting the size of the list
    override fun getItemCount() = reminderList.size

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder= reminderList[position]

        holder.titleTextView.text = reminder.name

        // Setting the click listeners for the update button
        holder.updateButton.setOnClickListener{
            showUpdateDialog(reminder, position)
        }

        // Setting the click listener for the delete button
        holder.deleteButton.setOnClickListener{
            val documentId = reminder.id
            Log.d(TAG, "Delete button clicked for document with ID: $documentId")

            if (documentId != null) {
                showDeleteConfirmationDialog(documentId, position)
            }
        }

        // Setting the click listener for the item itself
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, MindMapActivity::class.java)
//            // Passing the mind map item title to the mind map activity
//            intent.putExtra("mindMapTitle", mindMap.title)
//            context.startActivity(intent)
//        }
    }

    private fun showUpdateDialog(reminder: Reminder, position: Int){
        val oldTitle = reminder.name

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_reminder, null)
        val builder = AlertDialog.Builder(context).setView(dialogView)
        val editTextName = dialogView.findViewById<EditText>(R.id.dialogEditReminderItemEditText)
        editTextName.setText(oldTitle)
        val editTime = dialogView.findViewById<TimePicker>(R.id.time_picker)
        val editDate = dialogView.findViewById<DatePicker>(R.id.date_picker)

        val dialog = builder.create()

        // Setting the click listeners for the the confirm button
        dialogView.findViewById<Button>(R.id.dialogEditButtonConfirm).setOnClickListener {
            val newName = editTextName.text.toString()
            val newHour = editTime.hour
            val newMinute = editTime.minute
            val newDay = editDate.dayOfMonth
            val newMonth = editDate.month
            val newYear = editDate.year

            // Update title in firebase
            updateReminderTitle(reminder, newName, newHour, newMinute, newDay, newMonth, newYear, position)
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
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_deletion_reminder, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        val reminderIdTextView = dialogView.findViewById<TextView>(R.id.reminderIdTextView)
        reminderIdTextView.text = documentId

        dialogView.findViewById<Button>(R.id.dialogConfirmDeletionButtonYes).setOnClickListener {
            deleteReminderFromFirestore(documentId, position)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.dialogConfirmDeletionButtonNo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Update title method
    private fun updateReminderTitle(reminder: Reminder, newName: String, newHour: Int, newMinute: Int, newDay: Int, newMonth: Int, newYear: Int,  position: Int) {
        val documentId = reminder.id ?: return

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("reminderTemp").document(documentId)

        docRef.update("title", newName, newHour, newMinute, newDay, newMonth, newYear)
            .addOnSuccessListener {
                reminder.name = newName
                reminder.hour = newHour
                reminder.minute = newMinute
                reminder.day = newDay
                reminder.month = newMonth
                reminder.year = newYear
                notifyItemChanged(position)
                Log.d(TAG, "Reminder updated successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating reminder item", e)
            }
    }

    // Delete mind map method
    private fun deleteReminderFromFirestore(documentId: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("reminderTemp").document(documentId)

        docRef.delete()
            .addOnSuccessListener {
                // Safely remove the mind map at the specified position from the local list.
                // Created a mutable copy to avoid potential issues with concurrent modification.
                reminderList = reminderList.toMutableList().also {
                    if (position in 0 until it.size) it.removeAt(position)
                }
                // Remove the mind map from the adapter
                notifyItemRemoved(position)
                // Notify the adapter that the list has changed
                notifyItemChanged(position, reminderList.size)
//                notifyItemRangeChanged(position, itemCount - position)
                Log.d(TAG, "Reminder successfully deleted!")

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document with ID: $documentId", e)
            }

    }
}


