package com.extrastudios.docscanner.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.CROP_IMAGE_KEY
import com.extrastudios.docscanner.utils.FileUtils
import com.extrastudios.docscanner.utils.pdfDirectory
import com.extrastudios.docscanner.utils.showSnackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.theartofdev.edmodo.cropper.CropImageView.CropResult
import kotlinx.android.synthetic.main.activity_crop_image_activity.*
import java.io.File
import java.util.*

class CropImageActivity : BaseActivity(), View.OnClickListener {

    private val mCroppedImageUris = HashMap<Int, Uri>()
    private var mCurrentImageIndex = 0
    private var mImages: ArrayList<String>? = null
    private var mCurrentImageEdited = false
    private var mFinishedClicked = false

    companion object {
        fun getStartIntent(context: Context?, uris: ArrayList<String>): Intent {
            val intent = Intent(context, CropImageActivity::class.java)
            intent.putExtra(CROP_IMAGE_KEY, uris)
            return intent
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        cropButton.setOnClickListener(this)
        rotateButton.setOnClickListener(this)
        nextimageButton.setOnClickListener(this)
        previousImageButton.setOnClickListener(this)
        setUpCropImageView()
        mImages = intent.getStringArrayListExtra(CROP_IMAGE_KEY)
        mFinishedClicked = false
        for (i in mImages!!.indices) mCroppedImageUris[i] = Uri.fromFile(File(mImages!![i]))
        if (mImages!!.size == 0) finish()
        setImage(0)
    }


    private fun cropButtonClicked() {
        mCurrentImageEdited = false
        val folder = File(preferencesService.storageLocation + pdfDirectory)
        val uri = cropImageView!!.imageUri
        if (uri == null) {
            showSnackbar(R.string.error_uri_not_found)
            return
        }
        val path = uri.path
        var filename = "cropped_im"
        if (path != null) filename = "cropped_" + FileUtils.getFileName(path)
        val file = File(folder, filename)
        cropImageView!!.saveCroppedImageAsync(Uri.fromFile(file))
    }

    private fun rotateButtonClicked() {
        mCurrentImageEdited = true
        cropImageView!!.rotateImage(90)
    }

    private fun nextImageClicked() {
        if (mImages!!.size == 0) return
        if (!mCurrentImageEdited) {
            mCurrentImageIndex = (mCurrentImageIndex + 1) % mImages!!.size
            setImage(mCurrentImageIndex)
        } else {
            showSnackbar(R.string.save_first)
        }
    }

    private fun prevImgBtnClicked() {
        if (mImages!!.size == 0) return
        if (!mCurrentImageEdited) {
            if (mCurrentImageIndex == 0) {
                mCurrentImageIndex = mImages!!.size
            }
            mCurrentImageIndex = (mCurrentImageIndex - 1) % mImages!!.size
            setImage(mCurrentImageIndex)
        } else {
            showSnackbar(R.string.save_first)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_crop_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return true
            }
            R.id.action_done -> {
                mFinishedClicked = true
                cropButtonClicked()
                return true
            }
            R.id.action_skip -> {
                mCurrentImageEdited = false
                nextImageClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpCropImageView() {
        val folder =File(preferencesService.storageLocation+pdfDirectory)
        if (!folder.exists()){
            folder.mkdirs()
        }

        cropImageView!!.setOnCropImageCompleteListener { view: CropImageView?, result: CropResult ->
            mCroppedImageUris[mCurrentImageIndex] = result.uri
            cropImageView!!.setImageUriAsync(mCroppedImageUris[mCurrentImageIndex])
            if (mFinishedClicked) {
                val intent = Intent()
                intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, mCroppedImageUris)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setImage(index: Int) {
        mCurrentImageEdited = false
        if (index < 0 || index >= mImages!!.size) return
        imagecount!!.text =
            getString(R.string.cropImage_activityTitle) + " " + (index + 1) + " of " + mImages!!.size
        cropImageView!!.setImageUriAsync(mCroppedImageUris[index])
    }

    override fun onClick(v: View?) {
        when (v) {
            cropButton -> {
                cropButtonClicked()
            }
            rotateButton -> {
                rotateButtonClicked()
            }
            nextimageButton -> {
                nextImageClicked()
            }
            previousImageButton -> {
                prevImgBtnClicked()
            }
        }
    }


    override fun getLayout(): Int {
        return R.layout.activity_crop_image_activity
    }
}