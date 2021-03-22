package com.extrastudios.docscanner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.database.entity.History
import com.extrastudios.docscanner.databinding.ItemHistoryBinding
import com.extrastudios.docscanner.utils.inflate


class HistoryAdapter(
    private val historyList: List<History>,
    private val onHistoryClick: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryAdapter.HistoryHolder {
        val viewLayout: ItemHistoryBinding = parent.inflate(R.layout.item_history)
        return HistoryHolder(viewLayout)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.HistoryHolder, position: Int) {
        val history = historyList[position]
        holder.bindingView.item = history
        holder.itemView.setOnClickListener {
            onHistoryClick(history)
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    inner class HistoryHolder(var bindingView: ItemHistoryBinding) :
        RecyclerView.ViewHolder(bindingView.root)
}

