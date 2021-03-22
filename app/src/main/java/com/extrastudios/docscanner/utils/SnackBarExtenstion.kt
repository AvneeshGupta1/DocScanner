package com.extrastudios.docscanner.utils

import android.app.Activity
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.extrastudios.docscanner.R
import com.google.android.material.snackbar.Snackbar
import java.io.File


fun Activity.showSnackbar(resID: Int) {
    Snackbar.make(findViewById(R.id.content), resID, Snackbar.LENGTH_LONG).show()
}

fun Activity.showSnackbar(resID: String?) {
    Snackbar.make(findViewById(R.id.content), resID!!, Snackbar.LENGTH_LONG).show()
}

fun Fragment.showSnackbar(resID: Int) {
    Snackbar.make(activity!!.findViewById(R.id.content), resID, Snackbar.LENGTH_LONG).show()
}

fun Fragment.showSnackbar(resID: String?) {
    Snackbar.make(activity!!.findViewById(R.id.content), resID!!, Snackbar.LENGTH_LONG).show()
}

fun Activity.getSnackbarwithAction(resID: Int): Snackbar {
    return Snackbar.make(findViewById(R.id.content), resID, Snackbar.LENGTH_LONG)
}

fun getSnackbarwithAction(view: View, resID: Int): Snackbar {
    return Snackbar.make(view, resID, Snackbar.LENGTH_LONG)
}

val defaultStorageLocation: String
    get() {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath, pdfDirectory)
        if (!dir.exists()) {
            val isDirectoryCreated = dir.mkdir()
            if (!isDirectoryCreated) {
                Log.e("Error", "Directory could not be created")
            }
        }
        return dir.absolutePath + PATH_SEPERATOR
    }


@Throws(NumberFormatException::class)
fun String.parseIntOrDefault(def: Int = 0): Int {
    return if (isEmpty()) def else toString().toInt()
}