package com.extrastudios.docscanner.interfaces

import java.util.*

interface ExtractImagesListener {
    fun resetView()
    fun extractionStarted()
    fun updateView(imageCount: Int, outputFilePaths: ArrayList<String>)
}