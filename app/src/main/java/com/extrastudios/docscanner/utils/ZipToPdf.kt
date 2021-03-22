package com.extrastudios.docscanner.utils

import android.app.Activity
import android.net.Uri
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList

class ZipToPdf {
    fun convertZipToPDF(
        path: String,
        context: Activity,
        storageLocation: String,
        onImageUri: (ArrayList<Uri>) -> Unit
    ) {
        var bufferedOutputStream: BufferedOutputStream
        val imageUris = ArrayList<Uri>()
        (context as HomeActivity).makeAndClearTemp()
        val dest = storageLocation + pdfDirectory + tempDirectory
        try {
            val fileInputStream = FileInputStream(path)
            val zipInputStream = ZipInputStream(BufferedInputStream(fileInputStream))
            var zipEntry: ZipEntry?
            var folderPrefix = 0
            while (zipInputStream.nextEntry.also { e -> zipEntry = e } != null) {
                val zipEntryName = zipEntry?.name?.toLowerCase(Locale.ROOT)
                if (zipEntry != null && zipEntry!!.isDirectory) {
                    folderPrefix++
                } else if (zipEntryName != null && zipEntryName.endsWith(".jpg") || zipEntryName!!.endsWith(
                        ".jpeg"
                    ) || zipEntryName.endsWith(".png")
                ) {
                    var newFileName = "/$zipEntryName"
                    val index = zipEntryName.lastIndexOf("/")
                    /*index will be -1 when image is in just inside the zip
                     * and not inside some folder*/if (index != -1) newFileName =
                        zipEntryName.substring(index)
                    if (folderPrefix != 0) newFileName =
                        newFileName.replace("/", "/$folderPrefix- ")
                    val newFile = File(dest + newFileName)
                    imageUris.add(Uri.fromFile(newFile))
                    val buffer = ByteArray(BUFFER_SIZE)
                    val fileOutputStream = FileOutputStream(newFile)
                    bufferedOutputStream = BufferedOutputStream(fileOutputStream, BUFFER_SIZE)
                    var count: Int
                    while (zipInputStream.read(buffer, 0, BUFFER_SIZE).also { count = it } != -1) {
                        bufferedOutputStream.write(buffer, 0, count)
                    }
                    bufferedOutputStream.flush()
                    bufferedOutputStream.close()
                }
            }
            zipInputStream.close()
            if (imageUris.size == 0) {
                context.showSnackbar(R.string.error_no_image_in_zip)
                return
            }
            onImageUri(imageUris)
            //(context as MainActivity).convertImagesToPdf(imageUris)
        } catch (e: IOException) {
            e.printStackTrace()
            context.showSnackbar(R.string.error_open_file)
        }
    }


    private object SingletonHolder {
        val INSTANCE = ZipToPdf()
    }

    companion object {
        private const val BUFFER_SIZE = 4096
        val instance: ZipToPdf
            get() = SingletonHolder.INSTANCE
    }
}