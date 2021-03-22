package com.extrastudios.docscanner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.BrushItemAdapter.BrushItemViewHolder
import com.extrastudios.docscanner.model.BrushItem
import kotlinx.android.synthetic.main.brush_color_item.view.*

class BrushItemAdapter(
    private val brushColorItemList: ArrayList<BrushItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BrushItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrushItemViewHolder {
        val mView =
            LayoutInflater.from(parent.context).inflate(R.layout.brush_color_item, parent, false)
        return BrushItemViewHolder(mView)
    }

    override fun onBindViewHolder(holder: BrushItemViewHolder, position: Int) {
        val color = brushColorItemList[position].color

        if (position == brushColorItemList.size - 1) holder.itemView.doodle_color.background =
            holder.itemView.context.resources.getDrawable(color)
        else holder.itemView.doodle_color.setBackgroundColor(
            holder.itemView.context.resources.getColor(
                color
            )
        )

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return brushColorItemList.size
    }

    class BrushItemViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView)
}