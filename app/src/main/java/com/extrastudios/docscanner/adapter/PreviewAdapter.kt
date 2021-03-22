package com.extrastudios.docscanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.extrastudios.docscanner.R
import kotlinx.android.synthetic.main.pdf_preview_item.view.*
import java.io.File
import java.util.*

class PreviewAdapter(
    private val mContext: Context,
    private val previewItemList: ArrayList<String>
) : PagerAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val layout = mInflater.inflate(R.layout.pdf_preview_item, view, false)
        val path = previewItemList[position]
        val fileLocation = File(path)
        Glide.with(mContext).load(fileLocation).into(layout.image)
        val fileNameString = fileLocation.name
        layout.tvFileName.text = fileNameString
        view.addView(layout, 0)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return previewItemList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return String.format(
            mContext.resources.getString(R.string.showing_image),
            position + 1,
            previewItemList.size
        )
    }

    fun setData(images: ArrayList<String>) {
        previewItemList.clear()
        previewItemList.addAll(images)
    }

}