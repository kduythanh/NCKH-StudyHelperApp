package com.example.nlcs.adapter.flashcard

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.data.dao.CardDAO
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ItemSetFolderBinding
import com.example.nlcs.ui.activities.set.ViewSetActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetFolderViewAdapter(
    private val flashcardList: ArrayList<FlashCard>,
    private val isSelect: Boolean = false,
    private val folderId: String = ""
) : RecyclerView.Adapter<SetFolderViewAdapter.SetFolderViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetFolderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSetFolderBinding.inflate(layoutInflater, parent, false)
        return SetFolderViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SetFolderViewHolder, position: Int) {
        val flashcard = flashcardList[position]
        val folderDAO = FolderDAO(holder.itemView.context)

        holder.binding.setNameTv.text = flashcard.name

        // Launch a coroutine to count cards and update UI
        CoroutineScope(Dispatchers.Main).launch {
            val cardDAO = CardDAO(holder.itemView.context)
            val count = flashcard.id?.let { cardDAO.countCardByFlashCardId(flashcard.id!!) } ?: 0
            holder.binding.termCountTv.text = "$count terms"

            if (isSelect) {
                val isInFolder = flashcard.id?.let { folderDAO.isFlashCardInFolder(folderId,
                    flashcard.id!!
                ) }
                holder.binding.setFolderItem.background = AppCompatResources.getDrawable(
                    holder.itemView.context,
                    if (isInFolder == true) {
                        R.drawable.background_select
                    } else {
                        R.drawable.background_unselect
                    }
                )
            }
        }

        holder.binding.setFolderItem.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (isSelect) {
                    val isInFolder = flashcard.id?.let { folderDAO.isFlashCardInFolder(folderId,
                        flashcard.id!!
                    ) }
                    if (isInFolder == true) {
                        flashcard.id?.let { folderDAO.removeFlashCardFromFolder(folderId,
                            flashcard.id!!
                        ) }
                        holder.binding.setFolderItem.background = AppCompatResources.getDrawable(
                            holder.itemView.context,
                            R.drawable.background_unselect
                        )
                    } else {
                        flashcard.id?.let { folderDAO.addFlashCardToFolder(folderId, flashcard.id!!) }
                        holder.binding.setFolderItem.background = AppCompatResources.getDrawable(
                            holder.itemView.context,
                            R.drawable.background_select
                        )
                    }
                } else {
                    val intent = Intent(holder.itemView.context, ViewSetActivity::class.java).apply {
                        putExtra("id", flashcard.id)
                    }
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }




    override fun getItemCount(): Int {
        return flashcardList.size
    }

    class SetFolderViewHolder(val binding: ItemSetFolderBinding) : RecyclerView.ViewHolder(binding.root)
}