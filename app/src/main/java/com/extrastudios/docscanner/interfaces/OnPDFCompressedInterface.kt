package com.extrastudios.docscanner.interfaces

interface OnPDFCompressedInterface {
    fun pdfCompressionStarted()
    fun pdfCompressionEnded(path: String, success: Boolean)
}