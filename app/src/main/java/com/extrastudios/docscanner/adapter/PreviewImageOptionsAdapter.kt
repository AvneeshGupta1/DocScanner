package com.extrastudios.docscanner.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.databinding.ItemPreviewImageOptionsBinding
import com.extrastudios.docscanner.model.PreviewImageOptionItem
import com.extrastudios.docscanner.utils.inflate
import java.util.*

class PreviewImageOptionsAdapter(
    private val optionsList: ArrayList<PreviewImageOptionItem>,
    private val onPreviewImageClick: (Int) -> Unit
) : RecyclerView.Adapter<PreviewImageOptionsAdapter.PreviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val view: ItemPreviewImageOptionsBinding =
            parent.inflate(R.layout.item_preview_image_options)
        return PreviewViewHolder(view)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        val previewItem = optionsList[position]
        holder.bindingView.item = previewItem
        holder.itemView.setOnClickListener {
            onPreviewImageClick(position)
        }
    }

    override fun getItemCount(): Int {
        return optionsList.size
    }

    class PreviewViewHolder internal constructor(var bindingView: ItemPreviewImageOptionsBinding) :
        RecyclerView.ViewHolder(bindingView.root)
}