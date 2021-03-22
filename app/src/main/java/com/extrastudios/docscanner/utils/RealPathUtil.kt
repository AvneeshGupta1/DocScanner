package com.extrastudios.docscanner.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log

class RealPathUtil {

    fun getRealPath(context: Context, fileUri: Uri): String? {
        return getRealPathFromURI_API19(context, fileUri)
    }

    private fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
        var path: String? = null
        // DocumentProvider
        if (isDriveFile(uri)) {
            return null
        }
        if (DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    path = if ("primary".equals(type, ignoreCase = true)) {
                        if (split.size > 1) {
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        } else {
                            Environment.getExternalStorageDirectory().toString() + "/"
                        }
                    } else {
                        "storage" + "/" + docId.replace(":", "/")
                    }
                }
                isRawDownloadsDocument(uri) -> {
                    path = getDownloadsDocumentPath(context, uri, true)
                }
                isDownloadsDocument(uri) -> {
                    path = getDownloadsDocumentPath(context, uri, false)
                }
            }
        }
        return path
    }

    /**
     * Get a file path from an Uri that points to the Downloads folder.
     *
     * @param context       The context
     * @param uri           The uri to query
     * @param hasSubFolders The flag that indicates if the file is in the root or in a subfolder
     * @return The absolute file path
     */
    private fun getDownloadsDocumentPath(
        context: Context,
        uri: Uri,
        hasSubFolders: Boolean
    ): String? {
        val fileName = getFilePath(context, uri)
        val subFolderName = if (hasSubFolders) getSubFolders(uri) else ""
        if (fileName != null) {
            return if (subFolderName != null) Environment.getExternalStorageDirectory()
                .toString() + "/Download/" + subFolderName + fileName else Environment.getExternalStorageDirectory()
                .toString() + "/Download/" + fileName
        }
        val id = DocumentsContract.getDocumentId(uri)
        var path: String? = null
        if (!TextUtils.isEmpty(id)) {
            if (id.startsWith("raw:")) {
                path = id.replaceFirst("raw:".toRegex(), "")
            }
            try {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    id.toLong()
                )
                path = getDataColumn(context, contentUri, null, null)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        return path
    }

    /**
     * Get all the subfolders from an Uri.
     *
     * @param uri The uri
     * @return A string containing all the subfolders that point to the final file path
     */
    private fun getSubFolders(uri: Uri): String? {
        val replaceChars =
            uri.toString().replace("%2F", "/").replace("%20", " ").replace("%3A", ":")
        // searches for "Download" to get the directory path
        // for example, if the file is inside a folder "test" in the Download folder, this method
        // returns "test/"
        val components = replaceChars.split("/").toTypedArray()
        val sub5 = components[components.size - 2]
        val sub4 = components[components.size - 3]
        val sub3 = components[components.size - 4]
        val sub2 = components[components.size - 5]
        val sub1 = components[components.size - 6]
        return if (sub1 == "Download") {
            "$sub2/$sub3/$sub4/$sub5/"
        } else if (sub2 == "Download") {
            "$sub3/$sub4/$sub5/"
        } else if (sub3 == "Download") {
            "$sub4/$sub5/"
        } else if (sub4 == "Download") {
            "$sub5/"
        } else {
            null
        }
    }

    /**
     * Get the file path (without subfolders if any)
     *
     * @param context The context
     * @param uri     The uri to query
     * @return The file path
     */
    private fun getFilePath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = "_data"
        val projection = arrayOf(column)
        var path: String? = null
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(column)
                        path = cursor.getString(index)
                    }
                }
        } catch (e: Exception) {
            Log.e("Error", " " + e.message)
        }
        return path
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * This function is used to check for a drive file URI.
     *
     * @param uri - input uri
     * @return true, if is google drive uri, otherwise false
     */
    private fun isDriveFile(uri: Uri): Boolean {
        return if ("com.google.android.apps.docs.storage" == uri.authority) true else "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check
     * @return True if is a raw downloads document, otherwise false
     */
    private fun isRawDownloadsDocument(uri: Uri): Boolean {
        val uriToString = uri.toString()
        return uriToString.contains("com.android.providers.downloads.documents/document/raw")
    }

    private object SingletonHolder {
        val INSTANCE = RealPathUtil()
    }

    companion object {
        val instance: RealPathUtil
            get() = SingletonHolder.INSTANCE
    }
}