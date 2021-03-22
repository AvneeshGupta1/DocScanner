package com.extrastudios.docscanner.utils

interface OnPermissionListener {
    fun onStoragePermissionAllow()
    fun onCameraPermissionAllow()
    fun onPermissionCancel()
}