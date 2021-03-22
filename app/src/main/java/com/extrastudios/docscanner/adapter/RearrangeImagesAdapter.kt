package com.extrastudios.docscanner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.visibleIf
import kotlinx.android.synthetic.main.item_rearrange_images.view.*
import java.io.File
import java.util.*

class RearrangeImagesAdapter(
    private val mOnClickListener: OnClickListener,
    private var imagesUriList: ArrayList<String>
) : RecyclerView.Adapter<RearrangeImagesAdapter.RearrangeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RearrangeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rearrange_images, parent, false)
        return RearrangeViewHolder(view)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: RearrangeViewHolder, position: Int) {
        val imageFile = File(imagesUriList[position])
        holder.itemView.buttonUp.visibleIf(position != 0)
        holder.itemView.buttonDown.visibleIf(position != itemCount - 1 )
        Glide.with(holder.itemView.context).load(imageFile).into(holder.itemView.image)
        holder.itemView.pageNumber?.text = (position + 1).toString()
    }

    override fun getItemCount(): Int {
        return imagesUriList.size
    }

    fun positionChanged(images: ArrayList<String>) {
        imagesUriList = images
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
            itemView.buttonDown.setOnClickListener(this)
            itemView.buttonUp.setOnClickListener(this)
            itemView.removeImage.setOnClickListener(this)
        }
    }

}