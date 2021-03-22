package com.extrastudios.docscanner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.databinding.ItemSimpleRowBinding
import com.extrastudios.docscanner.utils.getFileName
import com.extrastudios.docscanner.utils.inflate


class BottomAdapter(
    private val itemList: List<String>,
    private var showLockIcon: Boolean = false,
    private val isMergeFiles: Boolean = false,
    private val onItemClick: (String, Int) -> Unit
) : RecyclerView.Adapter<BottomAdapter.ExcelViewHolder>() {

    private val selectedItems = ArrayList<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomAdapter.ExcelViewHolder {
        val viewLayout: ItemSimpleRowBinding = parent.inflate(R.layout.item_simple_row)
        return ExcelViewHolder(viewLayout)
    }

    override fun onBindViewHolder(holder: BottomAdapter.ExcelViewHolder, position: Int) {
        val path = itemList[position]
        holder.bindingView.item = path.getFileName()
        if (isMergeFiles) {
            if (selectedItems.contains(path)) holder.bindingView.fileName.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_ok_mark,
                0
            )
            else holder.bindingView.fileName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        }

        if (showLockIcon) {
            holder.bindingView.fileName.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_lock_black_24dp,
                0
            );
        }
        holder.itemView.setOnClickListener {
            onItemClick(path, position)
        }
    }

    fun setSelectedItemList(items: ArrayList<String>) {
        selectedItems.clear()
        selectedItems.addAll(items)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ExcelViewHolder(var bindingView: ItemSimpleRowBinding) :
        RecyclerView.ViewHolder(bindingView.root)
}

