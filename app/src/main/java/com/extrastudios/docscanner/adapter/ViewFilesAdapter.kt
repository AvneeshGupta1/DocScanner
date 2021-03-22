package com.extrastudios.docscanner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.databinding.ItemViewFilesBinding
import com.extrastudios.docscanner.model.PDFFile
import com.extrastudios.docscanner.utils.inflate
import kotlinx.android.synthetic.main.item_view_files.view.*
import java.io.File


class ViewFilesAdapter(
    private val pdfFilesList: List<PDFFile>,
    private val showCheckbox: Boolean,
    private val onFileClick: (Int, PDFFile) -> Unit,
    private val onCheckBoxChange: () -> Unit
) : RecyclerView.Adapter<ViewFilesAdapter.ViewFileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewFileViewHolder {
        val viewLayout: ItemViewFilesBinding = parent.inflate(R.layout.item_view_files)
        return ViewFileViewHolder(viewLayout)
    }

    override fun onBindViewHolder(holder: ViewFilesAdapter.ViewFileViewHolder, position: Int) {
        val pdfFile = pdfFilesList[position]
        holder.bindingView.item = pdfFile
        holder.bindingView.showCheckbox = showCheckbox
        holder.bindingView.checkbox.isChecked = pdfFile.isSelected
        holder.itemView.fileName.setOnClickListener {
            onFileClick(position, pdfFile)
        }
        holder.itemView.createdDate.setOnClickListener {
            onFileClick(position, pdfFile)
        }
        holder.itemView.fileSize.setOnClickListener {
            onFileClick(position, pdfFile)
        }
        holder.itemView.checkbox.setOnClickListener {
            pdfFile.isSelected = holder.itemView.checkbox.isChecked
            onCheckBoxChange()
        }
    }

    override fun getItemCount(): Int {
        return pdfFilesList.size
    }

    fun areItemsSelected(): Boolean {
        return pdfFilesList.any { it.isSelected }
    }

    fun isAllItemSelected(): Boolean {
        return pdfFilesList.filter { it.isSelected }.size == pdfFilesList.size
    }

    fun showMergeIcon(): Boolean {
        return pdfFilesList.filter { it.isSelected }.size > 1
    }

    fun getSelectedFiles(): ArrayList<File> {
        val list = ArrayList<File>()
        pdfFilesList.forEach {
            if (it.isSelected) {
                list.add(it.pdfFile)
            }
        }
        return list
    }

    fun getSelectedPaths(): ArrayList<String> {
        val list = ArrayList<String>()
        pdfFilesList.forEach {
            if (it.isSelected) {
                list.add(it.pdfFile.path)
            }
        }
        return list
    }

    class ViewFileViewHolder(var bindingView: ItemViewFilesBinding) :
        RecyclerView.ViewHolder(bindingView.root)
}

