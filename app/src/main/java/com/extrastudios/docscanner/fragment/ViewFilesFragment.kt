package com.extrastudios.docscanner.fragment

import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.ViewFilesAdapter
import com.extrastudios.docscanner.interfaces.MergeFilesListener
import com.extrastudios.docscanner.model.PDFFile
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.extrastudios.docscanner.viewmodel.ViewFilesViewModel
import kotlinx.android.synthetic.main.fragment_view_files.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import org.jetbrains.anko.alert
import java.io.File

class ViewFilesFragment : BaseFragments(), View.OnClickListener, MergeFilesListener {

    private var mMaterialDialog: MaterialDialog? = null
    private var menuItemDelete: MenuItem? = null
    private var menuItemShare: MenuItem? = null
    private var menuItemMerge: MenuItem? = null
    private var menuItemAll: MenuItem? = null
    private var viewFileAdapter: ViewFilesAdapter? = null
    private val mFileList = ArrayList<PDFFile>()
    private var pdfEncryptionUtility: PDFEncryptionUtility? = null
    private var pdfRotationUtils: PDFRotationUtils? = null
    private var watermarkUtils: WatermarkUtils? = null
    private var isFirstTime: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_files, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments == null) setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pdfEncryptionUtility = PDFEncryptionUtility(activity!!, preferencesService)
        pdfRotationUtils = PDFRotationUtils(activity!!)
        watermarkUtils = WatermarkUtils(activity!!)
        getStarted.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        checkStoragePermission()
        mRecyclerView
    }

    private fun showPopup() {
        if (arguments != null) {
            val dialogId = arguments!!.getInt(BUNDLE_DATA)
            activity?.showFilesInfoDialog(dialogId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_view_files_actions_if_selected, menu)
        menuItemAll = menu.findItem(R.id.select_all)
        menuItemDelete = menu.findItem(R.id.item_delete)
        menuItemShare = menu.findItem(R.id.item_share)
        menuItemMerge = menu.findItem(R.id.item_merge)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_all -> {
                if (viewFileAdapter!!.isAllItemSelected()) {
                    mFileList.forEach {
                        it.isSelected = false
                    }
                    viewFileAdapter!!.notifyDataSetChanged()
                } else {
                    mFileList.forEach {
                        it.isSelected = true
                    }
                    viewFileAdapter!!.notifyDataSetChanged()
                    onCheckboxChange()
                }
            }
            R.id.item_share -> {
                if (viewFileAdapter!!.areItemsSelected()) mFileUtils!!.shareMultipleFiles(
                    viewFileAdapter!!.getSelectedFiles()
                )
                else showSnackbar(R.string.snackbar_no_pdfs_selected)
            }
            R.id.item_merge -> {
                if (viewFileAdapter!!.getSelectedFiles().size > 1) {
                    val pdfpaths = viewFileAdapter!!.getSelectedPaths().toTypedArray()
                    openSaveDialog(null) {
                        MergePdf(
                            it,
                            preferencesService.storageLocation + pdfDirectory,
                            false,
                            null,
                            this,
                            preferencesService.masterPassword
                        ).execute(*pdfpaths)
                    }
                }
            }
            R.id.item_delete -> {
                if (viewFileAdapter!!.areItemsSelected()) showDeleteConfirmation()
                else showSnackbar(R.string.snackbar_no_pdfs_selected)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showDeleteConfirmation() {
        var messageAlert: Int = 0
        var title: Int = 0
        if (viewFileAdapter!!.getSelectedFiles().size > 1) {
            messageAlert = R.string.delete_alert_selected
            title = R.string.snackbar_files_deleted
        } else {
            messageAlert = R.string.delete_alert_singular
            title = R.string.snackbar_file_deleted
        }
        activity?.alert(messageAlert, title) {
            positiveButton(android.R.string.ok) {
                deleteSelectedFiles()
            }
            negativeButton(android.R.string.cancel)
        }?.show()
    }

    private fun deleteSelectedFiles() {
        val selectedFiles = viewFileAdapter!!.getSelectedFiles()
        selectedFiles.forEach {
            if (it.exists()) {
                it.delete()
            }
            loadAllFilesFromFolder()
        }
    }

    private val mRecyclerView by lazy {
        allFilesRecyclerView.itemAnimator = DefaultItemAnimator()
        allFilesRecyclerView.setHasFixedSize(true)
        allFilesRecyclerView.setDivider()
        viewFileAdapter = ViewFilesAdapter(
            mFileList,
            arguments == null,
            { pos, item -> onFileClick(pos, item) }) { onCheckboxChange() }
        allFilesRecyclerView.adapter = viewFileAdapter
    }

    private fun loadAllFilesFromFolder() {
        progressBar.show()
        val viewFilesViewModel = ViewModelProvider(this).get(ViewFilesViewModel::class.java)
        viewFilesViewModel.getAllFilesFromFolder().observe(this, Observer {
            progressBar.hide()
            mFileList.clear()
            mFileList.addAll(it)
            viewFileAdapter?.notifyDataSetChanged()
            emptyStatusView.visibleIf(mFileList.isEmpty())
            allFilesRecyclerView.visibleIf(mFileList.isNotEmpty())

            menuItemDelete?.isVisible = mFileList.isNotEmpty()
            menuItemAll?.isVisible = mFileList.isNotEmpty()
            menuItemMerge?.isVisible = false
            if (mFileList.isNotEmpty() && isFirstTime) showPopup()
            isFirstTime = false
        })
    }

    private fun onCheckboxChange() {
        if (viewFileAdapter!!.isAllItemSelected()) {
            menuItemAll?.setIcon(R.drawable.ic_check_box_24dp)
        } else {
            menuItemAll?.setIcon(R.drawable.ic_check_box_outline_blank_24dp)
        }
        menuItemMerge?.isVisible = viewFileAdapter!!.showMergeIcon()
    }

    private fun onFileClick(position: Int, pdfFile: PDFFile) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        MaterialDialog.Builder(activity!!).title(R.string.title)
            .items(if (pdfFile.isEncrypted) R.array.items_view_remove_password else R.array.items_view_add_password)
            .itemsIds(R.array.itemIds)
            .itemsCallback { dialog: MaterialDialog?, view1: View?, which: Int, text: CharSequence? ->
                performOperation(which, position, pdfFile)
            }.show()
    }


    private fun performOperation(index: Int, position: Int, pdfFile: PDFFile) {
        when (index) {
            0 -> mFileUtils!!.openFile(pdfFile.pdfFile.path, FileUtils.FileType.e_PDF)
            1 -> deleteFile(pdfFile)
            2 -> onRenameFileClick(pdfFile)
            3 -> mFileUtils!!.printFile(pdfFile.pdfFile) { path, operation ->
                saveToHistory(
                    path,
                    operation
                )
            }
            4 -> mFileUtils!!.shareFile(pdfFile.pdfFile)
            5 -> pdfEncryptionUtility?.showDetails(pdfFile)
            6 -> if (pdfFile.isEncrypted) pdfEncryptionUtility?.removePassword(pdfFile.pdfFile.path) { path, operation ->
                saveToHistory(
                    path,
                    operation
                )
            } else pdfEncryptionUtility?.setPassword(pdfFile.pdfFile.path) { path, operation ->
                saveToHistory(
                    path,
                    operation
                )
            }
            7 -> pdfRotationUtils?.rotatePages(pdfFile.pdfFile.path) { path, operation ->
                saveToHistory(
                    path,
                    operation
                )
            }
            8 -> {
                if (pdfFile.isEncrypted) {
                    showSnackbar(R.string.encrypted_pdf)
                    return
                } else watermarkUtils?.setWatermark(pdfFile.pdfFile.path) { path, operation ->
                    saveToHistory(
                        path,
                        operation
                    )
                }
            }
        }
    }

    private fun deleteFile(pdfFile: PDFFile) {
        val files = ArrayList<PDFFile>()
        files.add(pdfFile)
        deleteFiles(files) {
            onDeleteConfirm(files)
        }
    }

    private fun onDeleteConfirm(files: ArrayList<PDFFile>) {
        files.forEach {
            val delete = File(it.pdfFile.path).delete()
            if (delete) {
                showSnackbar(R.string.snackbar_file_deleted)
                saveToHistory(it.pdfFile.path, OPERATION_DELETED)
            } else {
                showSnackbar(R.string.snackbar_file_not_deleted)
            }
        }
        loadAllFilesFromFolder()
    }


    private fun onRenameFileClick(pdfFile: PDFFile) {
        openSaveDialog(null) { renameFile(it, pdfFile) }
    }

    private fun renameFile(newName: String, oldPdfFile: PDFFile) {
        val oldFile: File = oldPdfFile.pdfFile
        val oldPath = oldFile.path
        val newFilename =
            oldPath.substring(0, oldPath.lastIndexOf('/')) + "/" + newName + pdfExtension
        val newFile = File(newFilename)
        if (oldFile.renameTo(newFile)) {
            showSnackbar(R.string.snackbar_file_renamed)
            oldPdfFile.pdfFile = newFile
            viewFileAdapter?.notifyDataSetChanged()
            saveToHistory(newFilename, OPERATION_RENAME)
        } else showSnackbar(R.string.snackbar_file_not_renamed)
    }


    private fun saveToHistory(finalOutput: String, operation: Int) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, operation).observe(this, Observer {
            loadAllFilesFromFolder()
        })

    }

    override fun resetValues(isPDFMerged: Boolean, path: String?) {
        mMaterialDialog!!.dismiss()
        if (isPDFMerged) {
            getSnackbarwithAction(
                main,
                R.string.pdf_merged
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils?.openFile(
                    path,
                    FileUtils.FileType.e_PDF
                )
            }.show()
            saveToHistory(path!!, OPERATION_CREATED)
            loadAllFilesFromFolder()
        } else showSnackbar(R.string.file_access_error)
    }

    override fun mergeStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }

    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadAllFilesFromFolder()
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }


    override fun onClick(v: View?) {
        when (v) {
            getStarted -> (activity as HomeActivity).onItemSelected(R.id.nav_home)
            btnPermission -> checkStoragePermission()
        }
    }


}