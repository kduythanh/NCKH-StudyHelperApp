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
            CoroutineScope(Dispatchers.Main).launch {
                if (folder.id?.let { it1 -> folderDAO.isFlashCardInFolder(it1, flashcardId) } == true) {
                    folder.id?.let { it1 -> folderDAO.removeFlashCardFromFolder(it1, flashcardId) }
                    Toast.makeText(
                        holder.itemView.context,
                        "Removed from ${folder.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    folder.id?.let { it1 -> folderDAO.addFlashCardToFolder(it1, flashcardId) }
                    Toast.makeText(
                        holder.itemView.context,
                        "Added to ${folder.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            updateBackground(holder, folder, folderDAO)
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