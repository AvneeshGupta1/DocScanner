package com.extrastudios.docscanner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.databinding.ListItemFilterBinding
import com.extrastudios.docscanner.model.FilterItem
import com.extrastudios.docscanner.utils.inflate
import java.util.*

class ImageFiltersAdapter(
    private val filterItemList: ArrayList<FilterItem>,
    private val onFilterClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageFiltersAdapter.ImageFilterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFilterViewHolder {
        val view: ListItemFilterBinding = parent.inflate(R.layout.list_item_filter)
        return ImageFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageFilterViewHolder, position: Int) {
        val filterItem = filterItemList[position]
        holder.bindingView.item = filterItem
        holder.itemView.setOnClickListener {
            onFilterClick(position)
        }
    }

    override fun getItemCount(): Int {
        return filterItemList.size
    }

    class ImageFilterViewHolder internal constructor(val bindingView: ListItemFilterBinding) :
        RecyclerView.ViewHolder(bindingView.root)

}