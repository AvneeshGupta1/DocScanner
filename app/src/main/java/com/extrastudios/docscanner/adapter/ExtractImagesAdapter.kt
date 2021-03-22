package com.extrastudios.docscanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.getFileName
import kotlinx.android.synthetic.main.item_image_extracted.view.*
import java.util.*

class ExtractImagesAdapter(
    private val mFilePaths: ArrayList<String>?,
    private val mOnClickListener: OnFileItemClickedListener
) : RecyclerView.Adapter<ExtractImagesAdapter.ViewMergeFilesHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewMergeFilesHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_extracted, parent, false)
        return ViewMergeFilesHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewMergeFilesHolder, position: Int) {
        val path = mFilePaths!![position]
        holder.itemView.fileName!!.text = path.getFileName()
        Glide.with(holder.itemView.context).load(path).into(holder.itemView.imagePreview)
        holder.itemView.setOnClickListener {
            mOnClickListener.onFileItemClick(path)
        }
    }

    override fun getItemCount(): Int {
        return mFilePaths?.size ?: 0
    }

    interface OnFileItemClickedListener {
        fun onFileItemClick(path: String?)
    }

    inner class ViewMergeFilesHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView)
}