package com.extrastudios.docscanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.MergeSelectedFilesAdapter.MergeSelectedFilesHolder
import com.extrastudios.docscanner.utils.FileUtils
import kotlinx.android.synthetic.main.item_merge_selected_files.view.*
import java.util.*

class MergeSelectedFilesAdapter(
    private val mFilePaths: ArrayList<String>,
    private val mOnClickListener: OnFileItemClickListener
) : RecyclerView.Adapter<MergeSelectedFilesHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergeSelectedFilesHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_merge_selected_files, parent, false)
        return MergeSelectedFilesHolder(itemView)
    }

    override fun onBindViewHolder(holder: MergeSelectedFilesHolder, position: Int) {
        val item = mFilePaths[position]
        holder.itemView.fileName!!.text = FileUtils.getFileName(item)

        holder.itemView.view_file.setOnClickListener {
            mOnClickListener.viewFile(item)
        }
        holder.itemView.remove.setOnClickListener {
            mOnClickListener.removeFile(item)
        }
        holder.itemView.up_file.setOnClickListener {
            if (position != 0) {
                mOnClickListener.moveUp(position)
            }
        }
        holder.itemView.down_file.setOnClickListener {
            if (mFilePaths.size != position + 1) {
                mOnClickListener.moveDown(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return mFilePaths.size
    }

    interface OnFileItemClickListener {
        fun viewFile(path: String?)
        fun removeFile(path: String?)
        fun moveUp(position: Int)
        fun moveDown(position: Int)
    }

    inner class MergeSelectedFilesHolder internal constructor(val item: View) :
        RecyclerView.ViewHolder(item)

}