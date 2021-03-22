package com.extrastudios.docscanner.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

class FileUriUtils private constructor() {
    private val mEXTERNALSTORAGEDOC = "com.android.externalstorage.documents"
    private val mISDOWNLOADDOC = "com.android.providers.downloads.documents"
    private val mISMEDIADOC = "com.android.providers.media.documents"
    private val mISGOOGLEPHOTODOC = "com.google.android.apps.photos.content"
    fun isWhatsappImage(uriAuthority: String): Boolean {
        return "com.whatsapp.provider.media" == uriAuthority
    }

    private fun checkURIAuthority(uri: Uri, toCheckWith: String): Boolean {
        return toCheckWith == uri.authority
    }

    private fun checkURI(uri: Uri?, toCheckWith: String): Boolean {
        return uri != null && uri.scheme != null && uri.scheme.equals(
            toCheckWith,
            ignoreCase = true
        )
    }

    private fun isDocumentUri(mContext: Context?, uri: Uri?): Boolean {
        var ret = false
        if (mContext != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(mContext, uri)
        }
        return ret
    }

    private fun getURIForMediaDoc(mContentResolver: ContentResolver, uri: Uri): String? {
        val documentId = DocumentsContract.getDocumentId(uri)
        val idArr = documentId.split(":").toTypedArray()
        if (idArr.size == 2) {
            // First item is document type.
            val docType = idArr[0]

            // Second item is document real id.
            val realDocId = idArr[1]

            // Get content uri by document type.
            var mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            when (docType) {
                "image" -> mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            // Get where clause with real document id.
            val whereClause = MediaStore.Images.Media._ID + " = " + realDocId
            return getImageRealPath(mContentResolver, mediaContentUri, whereClause)
        }
        return null
    }

    private fun getURIForDownloadDoc(mContentResolver: ContentResolver, uri: Uri): String {
        val documentId = DocumentsContract.getDocumentId(uri)
        // Build download uri.
        val downloadUri = Uri.parse("content://downloads/public_downloads")
        // Append download document id at uri end.
        val downloadUriAppendId = ContentUris.withAppendedId(downloadUri, documentId.toLong())
        return getImageRealPath(mContentResolver, downloadUriAppendId, null)
    }

    private fun getURIForExternalstorageDoc(uri: Uri): String? {
        val documentId = DocumentsContract.getDocumentId(uri)
        val idArr = documentId.split(":").toTypedArray()
        if (idArr.size == 2) {
            val type = idArr[0]
            val realDocId = idArr[1]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + realDocId
            }
        }
        return null
    }

    private fun getUriForDocumentUri(mContentResolver: ContentResolver, uri: Uri): String? {
        if (checkURIAuthority(uri, mISMEDIADOC)) {
            return getURIForMediaDoc(mContentResolver, uri)
        } else if (checkURIAuthority(uri, mISDOWNLOADDOC)) {
            return getURIForDownloadDoc(mContentResolver, uri)
        } else if (checkURIAuthority(uri, mEXTERNALSTORAGEDOC)) {
            return getURIForExternalstorageDoc(uri)
        }
        return null
    }

    fun getUriRealPathAboveKitkat(mContext: Context, uri: Uri?): String? {
        if (uri == null) return null
        val mContentResolver = mContext.contentResolver
        if (checkURI(uri, "content")) return if (checkURIAuthority(
                uri,
                mISGOOGLEPHOTODOC
            )
        ) uri.lastPathSegment else getImageRealPath(mContentResolver, uri, null)
        if (checkURI(uri, "file")) return uri.path
        return if (isDocumentUri(mContext, uri)) getUriForDocumentUri(
            mContentResolver,
            uri
        ) else null
    }


    private fun getImageRealPath(
        contentResolver: ContentResolver,
        uri: Uri,
        whereClause: String?
    ): String {
        var ret = ""
        val cursor = contentResolver.query(uri, null, whereClause, null, null)
        if (cursor != null) {
            val moveToFirst = cursor.moveToFirst()
            if (moveToFirst) {
                // Get columns name by uri type.
                val columnName = MediaStore.Images.Media.DATA

                // Get column index.
                val imageColumnIndex = cursor.getColumnIndex(columnName)
                if (imageColumnIndex == -1) return ret

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex)
                cursor.close()
            }
        }
        return ret
    }

    fun getFilePath(uri: Uri): String? {
        var path = uri.path ?: return null
        path = path.replace("/document/raw:", "")
        return path
    }

    private object SingletonHolder {
        val INSTANCE = FileUriUtils()
    }

    companion object {
        @JvmStatic
        val instance: FileUriUtils
            get() = SingletonHolder.INSTANCE
    }
}