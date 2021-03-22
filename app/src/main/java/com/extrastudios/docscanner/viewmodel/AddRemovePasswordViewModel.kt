package com.extrastudios.docscanner.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.utils.ACTION_ADD_PWD
import com.extrastudios.docscanner.utils.ACTION_REMOVE_PWD
import com.extrastudios.docscanner.utils.isPasswordProtected
import com.extrastudios.docscanner.utils.pdfExtension
import org.jetbrains.anko.doAsync
import java.io.File
import javax.inject.Inject


class AddRemovePasswordViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var preferencesService: PreferencesService

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    private val mFilePaths = ArrayList<String>()


    fun loadAllPdfFiles(action: Int): MutableLiveData<ArrayList<String>> {
        val response = MutableLiveData<ArrayList<String>>()
        mFilePaths.clear()
        doAsync {
            try {
                walkDir(Environment.getExternalStorageDirectory(), listOf(pdfExtension), action)
                response.postValue(mFilePaths)
            } catch (e: Exception) {
            }
        }
        return response
    }

    private fun walkDir(dir: File, extensions: List<String> = listOf(pdfExtension), action: Int) {
        val listFile = dir.listFiles()
        if (listFile != null) {
            for (aListFile in listFile) {
                if (aListFile.isDirectory) {
                    walkDir(aListFile, extensions, action)
                } else {
                    for (extension in extensions) {
                        if (aListFile.name.endsWith(extension)) {
                            val isPwdProtected = aListFile.isPasswordProtected()
                            if (action == ACTION_ADD_PWD) {
                                if (!isPwdProtected) mFilePaths.add(aListFile.absolutePath)
                            } else if (action == ACTION_REMOVE_PWD) {
                                if (isPwdProtected) mFilePaths.add(aListFile.absolutePath)
                            } else {
                                mFilePaths.add(aListFile.absolutePath)
                            }
                        }
                    }
                }
            }
        }
    }
}