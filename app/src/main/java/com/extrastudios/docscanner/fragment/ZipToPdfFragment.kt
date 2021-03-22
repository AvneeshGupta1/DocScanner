package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.utils.RealPathUtil
import com.extrastudios.docscanner.utils.ZipToPdf
import com.extrastudios.docscanner.utils.hide
import com.extrastudios.docscanner.utils.show
import kotlinx.android.synthetic.main.fragment_zip_to_pdf.*
import kotlinx.android.synthetic.main.storage_permission_view.*

class ZipToPdfFragment : BaseFragments(), View.OnClickListener {
    private var mPath: String? = null
    private val mPickFileCode = 10
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_zip_to_pdf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectFile.setOnClickListener(this)
        zip_to_pdf.setOnClickListener(this)
        checkStoragePermission()
        btnPermission.setOnClickListener(this)
    }

    private fun showFileChooser() {
        val folderPath = Environment.getExternalStorageDirectory().toString() + "/"
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        val myUri = Uri.parse(folderPath)
        intent.setDataAndType(myUri, "application/zip")
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.merge_file_select)),
            mPickFileCode
        )
    }

    @Throws(NullPointerException::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val isOk = resultCode == Activity.RESULT_OK && data != null && data.data != null
        if (!isOk) return
        if (requestCode == mPickFileCode) {
            mPath = RealPathUtil.instance.getRealPath(context!!, data!!.data!!)
            if (mPath != null) {
                zip_to_pdf!!.show()
            }
        }
    }

    private fun convertZipToPdf() {
        progressBar!!.show()
        selectFile!!.blockTouch()
        zip_to_pdf!!.blockTouch()
        ZipToPdf.instance.convertZipToPDF(mPath!!, activity!!, preferencesService.storageLocation) {
            (activity as HomeActivity).convertImagesToPdf(it)
        }
        progressBar!!.hide()
        selectFile!!.unblockTouch()
        zip_to_pdf!!.unblockTouch()
    }


    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
    }

    override fun onCameraPermissionAllow() {
    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }


    override fun onClick(v: View?) {
        when (v) {
            selectFile -> showFileChooser()
            zip_to_pdf -> convertZipToPdf()
            btnPermission -> checkStoragePermission()
        }
    }
}