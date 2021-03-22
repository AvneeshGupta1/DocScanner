package com.extrastudios.docscanner.interfaces

import android.graphics.Bitmap

interface OnPdfReorderedInterface {
    /**
     * Marks the initiation of pdf reorder operation
     */
    fun onPdfReorderStarted()

    /**
     * Called when PdfReorder is complete
     *
     * @param bitmaps All the pages of the pdf as bitmap .
     */
    fun onPdfReorderCompleted(bitmaps: MutableList<Bitmap>)

    /*
     * Called when the pdf reorder operation fails.
     */
    fun onPdfReorderFailed()
}