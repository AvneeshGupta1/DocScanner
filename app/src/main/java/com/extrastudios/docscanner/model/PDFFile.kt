package com.extrastudios.docscanner.model

import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class PDFFile(var pdfFile: File, var isEncrypted: Boolean) {

    var formattedSize = getPdfSize()
    var isSelected: Boolean = false

    private fun getPdfSize(): String {
        val size = pdfFile.length()
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMo = sizeKb * sizeKb
        val sizeGo = sizeMo * sizeKb
        val sizeTerra = sizeGo * sizeKb
        return when {
            size < sizeMo -> df.format((size / sizeKb).toDouble()) + " KB"
            size < sizeGo -> df.format((size / sizeMo).toDouble()) + " MB"
            size < sizeTerra -> df.format((size / sizeGo).toDouble()) + " GB"
            else -> ""
        }
    }

    fun getFormattedDate(): String {
        try {
            val simpleDateFormat = SimpleDateFormat("EEE, MMM dd 'at' HH:mm", Locale.getDefault())
            return simpleDateFormat.format(pdfFile.lastModified())
        } catch (e: Exception) {

        }
        return ""
    }
}