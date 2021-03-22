package com.extrastudios.docscanner.interfaces

interface OnPDFCreatedInterface {
    fun onPDFCreationStarted()
    fun onPDFCreated(success: Boolean, path: String?)
}