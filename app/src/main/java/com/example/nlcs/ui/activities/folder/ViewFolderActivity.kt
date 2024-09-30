package com.example.nlcs.ui.activities.folder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.R
import com.example.nlcs.adapter.flashcard.SetFolderViewAdapter
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ActivityViewFolderBinding
import com.example.nlcs.databinding.DialogCreateFolderBinding
import com.example.nlcs.preferen.UserSharePreferences
import com.example.nlcs.ui.activities.learn.QuizFolderActivity
import com.example.nlcs.ui.activities.set.AddFlashCardActivity
import com.kennyc.bottomsheet.BottomSheetListener
import com.kennyc.bottomsheet.BottomSheetMenuDialogFragment
import com.saadahmedsoft.popupdialog.PopupDialog
import com.saadahmedsoft.popupdialog.Styles
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewFolderActivity : AppCompatActivity(), BottomSheetListener {
    private val binding by lazy { ActivityViewFolderBinding.inflate(layoutInflater) }
    private val folderDAO by lazy { FolderDAO(this) }
    private lateinit var adapter: SetFolderViewAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupToolbar()
        setupFolderDetails()
        setupRecyclerView()
        setupLearnButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFolderDetails() {
        // Retrieve folder ID from the intent
        val id = intent.getStringExtra("id")

        // Check if ID is not null
        if (id != null) {
            // Fetch folder details asynchronously
            folderDAO.getFolderById(id) { folder ->
                if (folder != null) {
                    // Update folder name UI
                    binding.folderNameTv.text = folder.name
                }

                // Fetch flashcards asynchronously and update the flashcard count
                folderDAO.getAllFlashCardByFolderId(id) { flashCards ->
                    binding.termCountTv.text = "${flashCards.size} flashcards"
                }
            }
        } else {
            // Handle the case where folder ID is null
            Log.e("FolderDetails", "Folder ID is null")
        }
    }





    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        val id = intent.getStringExtra("id")

        // Ensure the folder ID is not null
        if (id != null) {
            // Fetch flashcards asynchronously using the callback
            folderDAO.getAllFlashCardByFolderId(id) { flashCards ->
                // Once flashcards are retrieved, initialize the adapter
                adapter = SetFolderViewAdapter(flashCards, false)

                // Set up the RecyclerView layout manager and adapter
                val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                binding.setRv.layoutManager = linearLayoutManager
                binding.setRv.adapter = adapter

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()
            }
        } else {
            // Handle case where folder ID is null
            Log.e("RecyclerViewSetup", "Folder ID is null")
        }
    }


    private fun setupLearnButton() {
        val id = intent.getStringExtra("id")
        binding.learnThisFolderBtn.setOnClickListener {
            val newIntent = Intent(this, QuizFolderActivity::class.java)
            newIntent.putExtra("id", id)
            startActivity(newIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.vew_folder_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu) {
            BottomSheetMenuDialogFragment.Builder(
                context = this,
                sheet = R.menu.folder_menu,
                title = "Folder Menu",
                listener = this
            ).show(supportFragmentManager, "Menu")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSheetDismissed(
        bottomSheet: BottomSheetMenuDialogFragment,
        `object`: Any?,
        dismissEvent: Int
    ) {
        Log.d("TAG", "onSheetDismissed: ")
    }


    @SuppressLint("SetTextI18n")
    override fun onSheetItemSelected(
        bottomSheet: BottomSheetMenuDialogFragment,
        item: MenuItem,
        `object`: Any?
    ) {
        when (item.itemId) {
            R.id.edit_folder -> {
                CoroutineScope(Dispatchers.Main).launch { handleEditFolder() }
            }

            R.id.delete_folder -> {

                handleDeleteFolder()

            }

            R.id.add_set -> {
                handleAddSet()

            }

        }
    }

    private fun handleAddSet() {
        val id = intent.getStringExtra("id")
        val newIntent = Intent(this, AddFlashCardActivity::class.java)
        newIntent.putExtra("id_folder", id)
        startActivity(newIntent)
    }

    private fun handleDeleteFolder() {
        // Show a confirmation dialog
        PopupDialog.getInstance(this)
            .setStyle(Styles.STANDARD)
            .setHeading("Delete Folder")
            .setDescription("Are you sure you want to delete this folder?")
            .setPopupDialogIcon(R.drawable.ic_delete)
            .setCancelable(true)
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)

                    // Get the folder ID from the intent
                    val folderId = intent.getStringExtra("id")

                    if (folderId != null) {
                        // Attempt to delete the folder
                        val isDeleted = folderDAO.deleteFolder(folderId)

                        if (isDeleted) {
                            // Show success dialog if folder deletion was successful
                            PopupDialog.getInstance(this@ViewFolderActivity)
                                .setStyle(Styles.SUCCESS)
                                .setHeading(getString(R.string.success))
                                .setDescription(getString(R.string.delete_set_success))
                                .setCancelable(false)
                                .setDismissButtonText(getString(R.string.ok))
                                .showDialog(object : OnDialogButtonClickListener() {
                                    override fun onDismissClicked(dialog: Dialog) {
                                        super.onDismissClicked(dialog)
                                        finish() // Close the activity after successful deletion
                                    }
                                })
                        } else {
                            // Show error dialog if folder deletion failed
                            PopupDialog.getInstance(this@ViewFolderActivity)
                                .setStyle(Styles.FAILED)
                                .setHeading(getString(R.string.error))
                                .setDescription(getString(R.string.delete_set_error))
                                .setCancelable(true)
                                .showDialog(object : OnDialogButtonClickListener() {
                                    override fun onPositiveClicked(dialog: Dialog) {
                                        super.onPositiveClicked(dialog)
                                    }
                                })
                        }
                    }
                }

                override fun onNegativeClicked(dialog: Dialog?) {
                    super.onNegativeClicked(dialog)
                    dialog?.dismiss() // Dismiss dialog when cancel is clicked
                }
            })
    }


    @SuppressLint("SetTextI18n")
    private fun handleEditFolder() {
        val builder = AlertDialog.Builder(this)
        val dialogBinding = DialogCreateFolderBinding.inflate(layoutInflater)

        // Get the folder ID from the intent
        val id = intent.getStringExtra("id")

        // Fetch the folder asynchronously from Firestore
        if (id != null) {
            folderDAO.getFolderById(id) { folder ->
                if (folder != null) {
                    // Set the folder name and description in the dialog
                    dialogBinding.folderEt.setText(folder.name)
                    dialogBinding.descriptionEt.setText(folder.description)
                }
            }
        }

        builder.setView(dialogBinding.root)
        builder.setCancelable(true)
        val dialog = builder.create()

        dialogBinding.folderEt.requestFocus()
        dialogBinding.cancelTv.setOnClickListener {
            dialog.dismiss() // Dismiss the dialog when cancel is clicked
        }

        dialogBinding.okTv.setOnClickListener {
            val name = dialogBinding.folderEt.text.toString()
            val description = dialogBinding.descriptionEt.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter folder name", Toast.LENGTH_SHORT).show()
            } else {
                // Update the folder if it's valid
                id?.let { folderId ->
                    folderDAO.getFolderById(folderId) { folder ->
                        if (folder != null) {
                            folder.name = name
                            folder.description = description

                            // Call updateFolder() and check the Boolean result
                            if (folderDAO.updateFolder(folder)) {
                                // If update was successful
                                Toast.makeText(
                                    this,
                                    "Update folder successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Update UI with new folder name
                                binding.folderNameTv.text = folder.name

                                // Fetch and update flashcard count asynchronously
                                folderDAO.getAllFlashCardByFolderId(folderId) { flashCards ->
                                    runOnUiThread {
                                        binding.termCountTv.text = "${flashCards.size} flashcards"
                                    }
                                }

                                dialog.dismiss() // Close the dialog after success
                            } else {
                                // Handle the case where folder update failed
                                Toast.makeText(this, "Update folder failed", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                onBackPressedDispatcher.onBackPressed() // Go back if update fails
                            }
                        }
                    }
                }
            }
        }

        dialog.show() // Show the dialog after setting everything
    }



    override fun onSheetShown(bottomSheet: BottomSheetMenuDialogFragment, `object`: Any?) {
        Log.d("TAG", "onSheetShown: ")
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        // Retrieve the folder ID from the intent
        val id = intent.getStringExtra("id")

        // Fetch the flashcards asynchronously using the callback
        id?.let { folderId ->
            folderDAO.getAllFlashCardByFolderId(folderId) { flashCards ->
                // This block is called when flashcards are fetched

                // Set up the adapter with the fetched flashcards
                adapter = SetFolderViewAdapter(flashCards, false)

                // Set up the RecyclerView
                val linearLayoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                binding.setRv.layoutManager = linearLayoutManager
                binding.setRv.adapter = adapter

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()
            }
        }
        // Set up folder details
        setupFolderDetails()
    }
}


