package com.extrastudios.docscanner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.databinding.ItemHomeHeaderBinding
import com.extrastudios.docscanner.databinding.ItemHomeRowBinding
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeHeader
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.inflate

class HomeAdapter(
    private val homeItemList: List<CommonItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PARENT_HEADER) {
            val viewLayout: ItemHomeHeaderBinding = parent.inflate(R.layout.item_home_header)
            HeaderViewHolder(viewLayout)
        } else {
            val viewLayout: ItemHomeRowBinding = parent.inflate(R.layout.item_home_row)
            HomeHolder(viewLayout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val homeItem = homeItemList[position]
        if (holder is HomeHolder && homeItem is HomeItem) {
            holder.bindingView.item = homeItem
            holder.itemView.setOnClickListener {
                onItemClick(homeItem.type)
            }
        } else if (holder is HeaderViewHolder && homeItem is HomeHeader) {
            holder.bindingView.item = homeItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (homeItemList[position] is HomeHeader) {
            PARENT_HEADER
        } else PARENT_ITEM
    }

    override fun getItemCount(): Int {
        return homeItemList.size
    }

    fun isHeader(position: Int): Boolean {
        return homeItemList[position] is HomeHeader
    }

    fun isShow3Item(position: Int): Boolean {
        return position == 10 || position == 11 || position == 12 || position == 13 || position == 14 || position == 15 || position == 19 || position == 20 || position == 21
    }

    inner class HomeHolder(var bindingView: ItemHomeRowBinding) :
        RecyclerView.ViewHolder(bindingView.root)

    inner class HeaderViewHolder(var bindingView: ItemHomeHeaderBinding) :
        RecyclerView.ViewHolder(bindingView.root)

    companion object {
        private const val PARENT_HEADER = 0
        private const val PARENT_ITEM = 1
    }
}

