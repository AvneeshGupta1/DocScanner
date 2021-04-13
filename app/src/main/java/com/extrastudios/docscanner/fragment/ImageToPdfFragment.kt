package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.*
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.interfaces.OnPDFCreatedInterface
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.ImageToPdfViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.zhihu.matisse.Matisse
import kotlinx.android.synthetic.main.fragment_image_to_pdf.*
import kotlinx.android.synthetic.main.storage_camera_permission_view.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ImageToPdfFragment : BaseFragments(), View.OnClickListener, OnPDFCreatedInterface {

    @Inject
    lateinit var mPdfOptions: ImageToPDFOptions

    private val applyFilterCode = 10
    private val previewImageCode = 11
    private val rearrangeImageCode = 12
    private val getImagesCode = 13
    private var mMaterialDialog: MaterialDialog? = null
    private val excelToPdfOptions = ArrayList<CommonItem>()
    private var homeAdapter: HomeAdapter? = null
    private var mImagesUri = ArrayList<String>()
    private var mPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_to_pdf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        imageToPdfRecyclerView
        select_images.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        pdfCreate.setOnClickListener(this)
        pdfOpen.setOnClickListener(this)
        checkForImagesInBundle()
        if (mImagesUri.size > 0) {
            tvNoOfImages.text = String.format(getString(R.string.images_selected), mImagesUri.size)
            tvNoOfImages.show()
            mMorphButtonUtility!!.morphToSquare(pdfCreate, mMorphButtonUtility!!.integer())
            pdfCreate.isEnabled = true
            showSnackbar(R.string.successToast)
            pdfCreate.unblockTouch()
        } else {
            tvNoOfImages.hide()
            mMorphButtonUtility?.morphToGrey(pdfCreate, mMorphButtonUtility!!.integer())
            pdfCreate.isEnabled = false

        }
        checkStorageAndCameraPermission()
    }

    private val imageToPdfRecyclerView by lazy {
        enhancement_options_recycle_view.itemAnimator = DefaultItemAnimator()
        enhancement_options_recycle_view.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity, 2)
        enhancement_options_recycle_view.layoutManager = layoutManager
        homeAdapter = HomeAdapter(excelToPdfOptions) { item -> onItemClick(item) }
        enhancement_options_recycle_view.adapter = homeAdapter
    }

    private fun checkForImagesInBundle() {
        val bundle = arguments ?: return
        if (bundle.getBoolean(OPEN_SELECT_IMAGES)) selectImages()
        val uris: ArrayList<Parcelable> =
            bundle.getParcelableArrayList(getString(R.string.bundleKey)) ?: return
        for (p in uris) {
            val uriRealPath = mFileUtils!!.getUriRealPath(p as Uri)
            if (uriRealPath == null) {
                showSnackbar(R.string.whatsappToast)
            } else {
                mImagesUri.add(uriRealPath)
            }
        }
    }

    private fun selectImages() {
        ImageUtils.selectImages(this, getImagesCode)
    }

    private fun loadImageToPdfOptions() {
        val excelToPDFViewModel = ViewModelProvider(this).get(ImageToPdfViewModel::class.java)
        excelToPDFViewModel.getImageToPdfItems(activity!!, mPdfOptions).observe(this, Observer {
            excelToPdfOptions.clear()
            excelToPdfOptions.addAll(it)
            homeAdapter?.notifyDataSetChanged()
        })
    }

    private fun onItemClick(type: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        if (mImagesUri.size == 0) {
            showSnackbar(R.string.snackbar_no_images)
            return
        }

        when (type) {
            PASSWORD_PROTECTED_PDF -> {
                showSetPasswordDialog(mPdfOptions) {
                    loadImageToPdfOptions()
                }
            }
            EDIT_IMAGE -> {
                startActivityForResult(
                    CropImageActivity.getStartIntent(activity, mImagesUri),
                    CROP_IMAGE_ACTIVITY_REQUEST_CODE
                )
            }
            IMAGE_COMPRESSION -> {
                showImageCompressionDialog(mPdfOptions) { loadImageToPdfOptions() }
            }
            FILTER_IMAGE -> {
                startActivityForResult(
                    ImageEditor.getStartIntent(activity, mImagesUri),
                    applyFilterCode
                )
            }
            SET_PAGE_SIZE -> {
                showPageSizeDialog(mPdfOptions)
            }

            SET_IMAGE_SCALE_TYPE -> {
                showImageScaleTypeDialog(mPdfOptions)
            }
            PREVIEW_PDF -> {
                startActivityForResult(
                    PreviewActivity.getStartIntent(activity, mImagesUri),
                    previewImageCode
                )
            }
            BORDER_WIDTH -> {
                showBorderDialog(mPdfOptions) { loadImageToPdfOptions() }
            }
            REARRANGE_IMAGES -> {
                startActivityForResult(
                    RearrangeImages.getStartIntent(activity, mImagesUri),
                    rearrangeImageCode
                )
            }
            CREATE_GRAY_SCALE_PDF -> {
                createPdf(true)
            }
            ADD_MARGINS -> {
                showMarginDialog(mPdfOptions)
            }
            SHOW_PAGE_NUMBERS -> {
                showPageNumberStyleDialog(mPdfOptions)
            }
            ADD_WATER_MARK -> {
                addWatermark(mPdfOptions)
            }
            PAGE_COLOR -> {
                showPageColorDialog(mPdfOptions)
            }
        }
    }

    private fun createPdf(isGrayScale: Boolean) {
        openSaveDialog(null) { filename -> save(isGrayScale, filename) }
    }

    private fun save(isGrayScale: Boolean, filename: String) {
        mPdfOptions.imagesUri = mImagesUri
        mPdfOptions.imageScaleType = preferencesService.imageScaleType
        mPdfOptions.pageNumStyle = preferencesService.pageNumberStyle
        mPdfOptions.pageColor = preferencesService.pageColor
        mPdfOptions.masterPwd = preferencesService.masterPassword
        mPdfOptions.outFileName = filename
        if (isGrayScale) saveImagesInGrayScale()
        CreatePdf(mPdfOptions, preferencesService.storageLocation + pdfDirectory, this).execute()
    }

    private fun saveImagesInGrayScale() {
        val tempImageUri = ArrayList<String>()
        try {
            val sdCard = preferencesService.storageLocation + pdfDirectory
            val dir = File(sdCard + "PDFfilter")
            if (!dir.exists()) dir.mkdirs()
            val size: Int = mImagesUri.size
            for (i in 0 until size) {
                val fileName = String.format(
                    getString(R.string.filter_file_name),
                    System.currentTimeMillis().toString(),
                    i.toString() + "_grayscale"
                )
                val outFile = File(dir, fileName)
                val f = File(mImagesUri.get(i))
                val fis = FileInputStream(f)
                val bitmap = BitmapFactory.decodeStream(fis)
                val grayScaleBitmap = ImageUtils.instance.toGrayscale(bitmap)
                outFile.createNewFile()
                val bos = BufferedOutputStream(FileOutputStream(outFile), 1024 * 8)
                grayScaleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                bos.close()
                tempImageUri.add(outFile.absolutePath)
            }
            mImagesUri.clear()
            mImagesUri.addAll(tempImageUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        when (requestCode) {
            getImagesCode -> {
                mImagesUri.clear()
                mImagesUri.addAll(Matisse.obtainPathResult(data))
                if (mImagesUri.size > 0) {
                    tvNoOfImages.text =
                        String.format(getString(R.string.images_selected), mImagesUri.size)
                    tvNoOfImages.show()
                    showSnackbar(R.string.snackbar_images_added)
                    pdfCreate.isEnabled = true
                    pdfCreate.unblockTouch()
                }
                mMorphButtonUtility!!.morphToSquare(pdfCreate, mMorphButtonUtility!!.integer())
                pdfOpen.hide()
            }
            applyFilterCode -> {
                mImagesUri.clear()
                val mFilterUris: ArrayList<String> = data.getStringArrayListExtra(RESULT)?:ArrayList()
                val size: Int = mFilterUris.size - 1
                for (k in 0..size) mImagesUri.add(mFilterUris[k])
            }
            CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val croppedImageUris: HashMap<Int, Uri> =
                    data.getSerializableExtra(CropImage.CROP_IMAGE_EXTRA_RESULT) as HashMap<Int, Uri>
                for (i in mImagesUri.indices) {
                    if (croppedImageUris[i] != null) {
                        mImagesUri[i] = croppedImageUris[i]!!.path!!
                        showSnackbar(R.string.snackbar_imagecropped)
                    }
                }
            }
            previewImageCode -> {
                mImagesUri = data.getStringArrayListExtra(RESULT)?:ArrayList()
                if (mImagesUri.size > 0) {
                    tvNoOfImages.text = String.format(
                        resources.getString(R.string.images_selected),
                        mImagesUri.size
                    )
                } else {
                    tvNoOfImages.hide()
                    mMorphButtonUtility?.morphToGrey(pdfCreate, mMorphButtonUtility!!.integer())
                }
            }
            rearrangeImageCode -> {
                mImagesUri = data.getStringArrayListExtra(RESULT)?:ArrayList()
                if (mImagesUri.size > 0) {
                    tvNoOfImages.text = String.format(
                        resources.getString(R.string.images_selected),
                        mImagesUri.size
                    )
                    showSnackbar(R.string.images_rearranged);
                }
                if (mImagesUri.size == 0) {
                    tvNoOfImages.hide()
                    mMorphButtonUtility?.morphToGrey(pdfCreate, mMorphButtonUtility!!.integer())
                }
            }
        }

    }

    override fun onPDFCreationStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }

    override fun onPDFCreated(success: Boolean, path: String?) {
        mMaterialDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        if (!success) {
            showSnackbar(R.string.snackbar_folder_not_created)
            return
        }
        saveToHistory(path!!)
        activity!!.getSnackbarwithAction(R.string.snackbar_pdfCreated)
            .setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils!!.openFile(
                    path,
                    FileUtils.FileType.e_PDF
                )
            }.show()
        pdfOpen.show()
        mMorphButtonUtility!!.morphToSuccess(pdfCreate)
        pdfCreate.blockTouch()
        mImagesUri.clear()
        mPath = path
        reset()
    }

    private fun reset() {
        mPdfOptions.reset()
        loadImageToPdfOptions()
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
        (activity as HomeActivity).showAdIfLoad()
    }

    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadImageToPdfOptions()
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }

    override fun onClick(v: View?) {
        when (v) {
            btnPermission -> checkStorageAndCameraPermission()
            select_images -> selectImages()
            pdfCreate -> createPdf(false)
            pdfOpen -> {
                if (!mPath.isNullOrEmpty()) mFileUtils!!.openFile(mPath, FileUtils.FileType.e_PDF)
            }
        }
    }

}