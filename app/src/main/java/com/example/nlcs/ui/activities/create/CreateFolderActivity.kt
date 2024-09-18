package com.example.nlcs.ui.activities.create

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher.onBackPressed
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.R
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.data.model.Folder
import com.example.nlcs.databinding.ActivityCreateFolderBinding
import com.example.nlcs.ui.activities.folder.ViewFolderActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

class CreateFolderActivity : AppCompatActivity() {
    private var binding: ActivityCreateFolderBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFolderBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)

        setSupportActionBar(binding!!.toolbar)
        // Replace getOnBackPressedDispatcher() with onBackPressed() for back navigation
        binding!!.toolbar.setNavigationOnClickListener {
            onBackPressed()  // This will handle the back press
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tick, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.done) {
            val folderName = binding!!.folderEt.text.toString().trim { it <= ' ' }
            val description = binding!!.descriptionEt.text.toString().trim { it <= ' ' }
            if (folderName.isEmpty()) {
                binding!!.folderTil.error = ""
                binding!!.folderTil.helperText = "Folder name cannot be empty"
                binding!!.folderEt.requestFocus()
                return false
            } else {
                val folderId = genUUID()
                val createdAt = currentDate
                val updatedAt = currentDate

                val folder = Folder(folderId, folderName, description, createdAt, updatedAt)
                val folderDAO = FolderDAO(this)
                if (folderDAO.insertFolder(folder) > 0) {
                    Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this, ViewFolderActivity::class.java).putExtra(
                            "id",
                            folderId
                        )
                    )
                    finish()
                } else {
                    Toast.makeText(this, "Folder not created", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val currentDate: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDateNewApi
        } else {
            currentDateOldApi
        }


    private fun genUUID(): String {
        return UUID.randomUUID().toString()
    }

    @get:RequiresApi(api = Build.VERSION_CODES.O)
    private val currentDateNewApi: String
        get() {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return currentDate.format(formatter)
        }

    private val currentDateOldApi: String
        get() {
            @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("dd/MM/yyyy")
            return sdf.format(Date())
        }
}