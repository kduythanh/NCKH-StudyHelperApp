package com.example.nlcs.adapter.folder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.data.model.Folder
import com.example.nlcs.databinding.ItemFolderSelectBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FolderSelectAdapter(
    private val folderList: List<Folder>,
    private val flashcardId: String
) : RecyclerView.Adapter<FolderSelectAdapter.FolderSelectViewHolder>() {
    class FolderSelectViewHolder(
        val binding: ItemFolderSelectBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderSelectViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFolderSelectBinding.inflate(layoutInflater, parent, false)
        return FolderSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderSelectViewHolder, position: Int) {
        val folder = folderList[position]
        val folderDAO = FolderDAO(holder.itemView.context)
        holder.binding.folderNameTv.text = folder.name
        updateBackground(holder, folder, folderDAO)

        holder.binding.folderCv.setOnClickListener {
            // Launch a coroutine to handle suspend functions
            CoroutineScope(Dispatchers.Main).launch {
                // Check if the flashcard is in the folder using the suspend function
                val isInFolder =
                    folder.id?.let { it1 -> folderDAO.isFlashCardInFolder(it1, flashcardId) }

                if (isInFolder == true) {
                    // If the flashcard is in the folder, remove it using the suspend function
                    val removed =
                        folder.id?.let { it1 -> folderDAO.removeFlashCardFromFolder(it1, flashcardId) }
                    if (removed == true) {
                        //Toast.makeText(
                       //     holder.itemView.context,
                            ///"Removed from ${folder.name}",
                          //  Toast.LENGTH_SHORT
                       // ).show()
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed to remove from ${folder.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // If the flashcard is not in the folder, add it (assuming addFlashCardToFolder is not suspend)
                    folder.id?.let { it1 -> folderDAO.addFlashCardToFolder(it1, flashcardId) }
                   // Toast.makeText(
                      //  holder.itemView.context,
                     //   "Added to ${folder.name}",
                     //   Toast.LENGTH_SHORT
                  //  ).show()
                }
                // Update the background after the operation
                updateBackground(holder, folder, folderDAO)
            }
        }
    }


    private fun updateBackground(holder: FolderSelectViewHolder, folder: Folder, folderDAO: FolderDAO) {

        CoroutineScope(Dispatchers.Main).launch {
            if (folder.id?.let { folderDAO.isFlashCardInFolder(it, flashcardId) } == true) {
                holder.binding.folderCv.background =
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        com.example.nlcs.R.drawable.background_select
                    )
            } else {
                holder.binding.folderCv.background =
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        com.example.nlcs.R.drawable.background_unselect
                    )
            }
        }
    }

    override fun getItemCount(): Int {
        return folderList.size
    }
}