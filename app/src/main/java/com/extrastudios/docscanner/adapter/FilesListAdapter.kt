package com.extrastudios.docscanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.FilesListAdapter.ViewMergeFilesHolder
import com.extrastudios.docscanner.utils.getFileName
import com.extrastudios.docscanner.utils.hide
import kotlinx.android.synthetic.main.item_merge_files.view.*
import java.util.*

class FilesListAdapter(
    private val mFilePaths: ArrayList<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ViewMergeFilesHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewMergeFilesHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_merge_files, parent, false)
        return ViewMergeFilesHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewMergeFilesHolder, position: Int) {
        val item = mFilePaths[position]
        holder.itemView.fileName.text = item.getFileName()
        holder.itemView.encryptionImage.hide()
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int {
        return mFilePaths.size
    }

    inner class ViewMergeFilesHolder internal constructor(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!)
}