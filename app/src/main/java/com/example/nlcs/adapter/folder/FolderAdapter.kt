package com.example.nlcs.adapter.folder

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.data.model.Folder
import com.example.nlcs.databinding.ItemFolderBinding
import com.example.nlcs.ui.activities.folder.ViewFolderActivity

class FolderAdapter(private val context: Context, private val folders: ArrayList<Folder>) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemFolderBinding.inflate(inflater, parent, false)
        return FolderViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.binding.folderNameTv.text = folder.name
        holder.binding.folderCl.setOnClickListener { v: View? ->
            val intent = Intent(context, ViewFolderActivity::class.java)
            intent.putExtra("id", folder.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemFolderBinding = ItemFolderBinding.bind(itemView)
    }
}
