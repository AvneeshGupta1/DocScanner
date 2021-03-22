package com.extrastudios.docscanner.interfaces

interface MergeFilesListener {
    fun resetValues(isPDFMerged: Boolean, path: String?)
    fun mergeStarted()
}