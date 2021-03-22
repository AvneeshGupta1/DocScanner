package com.extrastudios.docscanner.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.goneIf
import kotlinx.android.synthetic.main.item_rearrange_images.view.*
import java.util.*

class RearrangePdfAdapter(
    private val mOnClickListener: OnClickListener,
    private var mBitmaps: ArrayList<Bitmap>
) : RecyclerView.Adapter<RearrangePdfAdapter.RearrangeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RearrangeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rearrange_images, parent, false)
        return RearrangeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RearrangeViewHolder, position: Int) {
        holder.itemView.buttonUp.goneIf(position == 0)
        holder.itemView.buttonDown.goneIf(position == itemCount - 1)
        holder.itemView.image.setImageBitmap(mBitmaps[position])
        holder.itemView.pageNumber.text = (position + 1).toString()
    }

    override fun getItemCount(): Int {
        return mBitmaps.size
    }

    fun positionChanged(images: ArrayList<Bitmap>) {
        mBitmaps = images
        notifyDataSetChanged()
    }

    interface OnClickListener {
        fun onUpClick(position: Int)
        fun onDownClick(position: Int)
        fun onRemoveClick(position: Int)
    }

    inner class RearrangeViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        override fun onClick(view: View) {
            when (view.id) {
                R.id.buttonUp -> mOnClickListener.onUpClick(adapterPosition)
                R.id.buttonDown -> mOnClickListener.onDownClick(adapterPosition)
                R.id.removeImage -> mOnClickListener.onRemoveClick(adapterPosition)
            }
        }

        init {
            itemView.buttonDown!!.setOnClickListener(this)
            itemView.buttonUp!!.setOnClickListener(this)
            itemView.removeImage!!.setOnClickListener(this)
        }
    }

}