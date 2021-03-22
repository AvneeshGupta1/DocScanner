package com.extrastudios.docscanner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.FAQAdapter.FAQViewHolder
import com.extrastudios.docscanner.databinding.ItemFaqBinding
import com.extrastudios.docscanner.model.FAQItem
import com.extrastudios.docscanner.utils.inflate
import kotlinx.android.synthetic.main.item_faq.view.*

class FAQAdapter(private val faqList: List<FAQItem>, private val onItemClicked: (Int) -> Unit) :
    RecyclerView.Adapter<FAQViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): FAQViewHolder {
        val view: ItemFaqBinding = parent.inflate(R.layout.item_faq)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: FAQViewHolder, position: Int) {
        val faqItem = faqList[position]
        viewHolder.bindingView.item = faqItem
        viewHolder.itemView.question.setOnClickListener {
            onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    class FAQViewHolder internal constructor(val bindingView: ItemFaqBinding) :
        RecyclerView.ViewHolder(bindingView.root)
}