package com.extrastudios.docscanner.utils

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.*

class PrintDocumentAdapterHelper internal constructor(private val mFile: File) :
    PrintDocumentAdapter() {
    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            val input: InputStream = FileInputStream(mFile.name)
            val output: OutputStream = FileOutputStream(destination.fileDescriptor)
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buf).also { bytesRead = it } > 0) output.write(buf, 0, bytesRead)
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            input.close()
            output.close()
        } catch (e: Exception) {
            //Catch exception
        }
    }

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val pdi = PrintDocumentInfo.Builder("myFile")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
        callback.onLayoutFinished(pdi, true)
    }

}