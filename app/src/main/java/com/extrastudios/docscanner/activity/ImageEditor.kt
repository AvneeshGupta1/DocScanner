package com.extrastudios.docscanner.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.BrushItemAdapter
import com.extrastudios.docscanner.adapter.ImageFiltersAdapter
import com.extrastudios.docscanner.model.BrushItem
import com.extrastudios.docscanner.model.FilterItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.ImageToPdfViewModel
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.PhotoFilter
import kotlinx.android.synthetic.main.activity_photo_editor.*
import java.io.File

class ImageEditor : BaseActivity(), View.OnClickListener {

    private val mImagePaths = ArrayList<String>()
    private var mFilterUris = ArrayList<String>()
    private var mFilterItems: ArrayList<FilterItem>? = null
    private var mBrushItems: ArrayList<BrushItem>? = null
    private var mDisplaySize = 0
    private var mCurrentImage = 0
    private var mFilterName: String? = null
    private var mClicked = true
    private var mClickedFilter = false
    private var mDoodleSelected = false
    private var mPhotoEditor: PhotoEditor? = null


    companion object {
        fun getStartIntent(context: Context?, uris: ArrayList<String>): Intent {
            val intent = Intent(context, ImageEditor::class.java)
            intent.putExtra(IMAGE_EDITOR_KEY, uris)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextimageButton.setOnClickListener(this)
        previousImageButton.setOnClickListener(this)
        savecurrent.setOnClickListener(this)
        resetCurrent.setOnClickListener(this)
        initValues()
    }


    private fun initValues() {
        mFilterUris = intent.getStringArrayListExtra(IMAGE_EDITOR_KEY)?:ArrayList()
        mDisplaySize = mFilterUris.size
        val viewModel = ViewModelProvider(this).get(ImageToPdfViewModel::class.java)
        mFilterItems = viewModel.getFilterItemsList(this)
        mBrushItems = viewModel.getBrushItemList()
        mImagePaths.addAll(mFilterUris)
        photoEditorView.source.setImageBitmap(BitmapFactory.decodeFile(mFilterUris[0]))
        changeAndShowImageCount(0)
        initRecyclerView()
        mPhotoEditor = PhotoEditor.Builder(this, photoEditorView).setPinchTextScalable(true).build()
        doodleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mPhotoEditor?.brushSize = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        mPhotoEditor?.brushSize = 30f
        mPhotoEditor?.setBrushDrawingMode(false)
    }

    private fun nextImg() {
        if (mClicked) {
            changeAndShowImageCount((mCurrentImage + 1) % mDisplaySize)
        } else showSnackbar(R.string.save_first)
    }

    private fun previousImg() {
        if (mClicked) {
            changeAndShowImageCount(mCurrentImage - 1 % mDisplaySize)
        } else showSnackbar(R.string.save_first)
    }

    private fun changeAndShowImageCount(count: Int) {
        if (count < 0 || count >= mDisplaySize) return
        mCurrentImage = count % mDisplaySize
        photoEditorView!!.source.setImageBitmap(BitmapFactory.decodeFile(mImagePaths[mCurrentImage]))
        imagecount!!.text =
            String.format(getString(R.string.showing_image), mCurrentImage + 1, mDisplaySize)
    }

    private fun saveC() {
        mClicked = true
        if (mClickedFilter || mDoodleSelected) {
            saveCurrentImage()
            showHideBrushEffect(false)
            mClickedFilter = false
            mDoodleSelected = false
        }
    }

    private fun resetCurrent() {
        mClicked = true
        val originalPath = mFilterUris[mCurrentImage]
        mImagePaths[mCurrentImage] = originalPath
        photoEditorView!!.source.setImageBitmap(BitmapFactory.decodeFile(originalPath))
        mPhotoEditor!!.clearAllViews()
        mPhotoEditor!!.undo()
    }


    private fun saveCurrentImage() {
        try {
            val sdCard = preferencesService.storageLocation + pdfDirectory
            val dir = File(sdCard + "PDFfilter")
            if (!dir.exists()) dir.mkdirs()
            val fileName = String.format(
                getString(R.string.filter_file_name),
                System.currentTimeMillis().toString(),
                mFilterName
            )
            val outFile = File(dir, fileName)
            val imagePath = outFile.absolutePath
            mPhotoEditor!!.saveAsFile(imagePath, object : OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    mImagePaths.removeAt(mCurrentImage)
                    mImagePaths.add(mCurrentImage, imagePath)
                    photoEditorView.source.setImageBitmap(BitmapFactory.decodeFile(mImagePaths[mCurrentImage]))
                    Toast.makeText(applicationContext, R.string.filter_saved, Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(exception: java.lang.Exception) {
                    Toast.makeText(
                        applicationContext,
                        R.string.filter_not_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        val adapter = ImageFiltersAdapter(mFilterItems!!) { index -> onFilterItemClick(index) }
        recyclerView.adapter = adapter


        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        doodle_colors!!.layoutManager = layoutManager2
        val brushItemAdapter = BrushItemAdapter(mBrushItems!!) { pos -> onItemClick(pos) }
        doodle_colors.adapter = brushItemAdapter
    }

    private fun onFilterItemClick(position: Int) {
        mClicked = position == 0
        if (position == 1) {
            mPhotoEditor =
                PhotoEditor.Builder(this, photoEditorView).setPinchTextScalable(true).build()
            if (doodleSeekBar!!.visibility == View.GONE && doodle_colors!!.visibility == View.GONE) {
                showHideBrushEffect(true)
            } else if (doodleSeekBar!!.visibility == View.VISIBLE && doodle_colors!!.visibility == View.VISIBLE) {
                showHideBrushEffect(false)
            }
            mFilterName = mFilterItems!![position].name
        } else {
            applyFilter(mFilterItems!![position].filter)
        }
    }


    private fun showHideBrushEffect(show: Boolean) {
        mPhotoEditor?.setBrushDrawingMode(show)
        doodleSeekBar?.visibleIf(show)
        doodle_colors?.visibleIf(show)
        mDoodleSelected = true
    }


    private fun applyFilter(filterType: PhotoFilter) {
        try {
            mPhotoEditor =
                PhotoEditor.Builder(this, photoEditorView).setPinchTextScalable(true).build()
            mPhotoEditor?.setFilterEffect(filterType)
            mFilterName = filterType.name
            mClickedFilter = filterType != PhotoFilter.NONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putStringArrayListExtra(RESULT, mImagePaths)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    fun onItemClick(position: Int) {
        val color = mBrushItems!![position].color
        if (position == mBrushItems!!.size - 1) {
            val colorPallete = MaterialDialog.Builder(this).title(R.string.choose_color_text)
                .customView(R.layout.color_pallete_layout, true).positiveText(R.string.ok)
                .negativeText(R.string.cancel).build()
            val mPositiveAction: View = colorPallete.getActionButton(DialogAction.POSITIVE)
            val colorPickerInput: ColorPickerView =
                colorPallete.customView!!.findViewById(R.id.color_pallete)
            mPositiveAction.isEnabled = true
            mPositiveAction.setOnClickListener { v: View? ->
                try {
                    doodleSeekBar?.setBackgroundColor(colorPickerInput.color)
                    mPhotoEditor?.brushColor = colorPickerInput.color
                } catch (e: Exception) {
                }
                colorPallete.dismiss()
            }
            colorPallete.show()
        } else {
            doodleSeekBar!!.setBackgroundColor(this.resources.getColor(color))
            mPhotoEditor!!.brushColor = this.resources.getColor(color)
        }
    }

    override fun onClick(view: View) {
        when (view) {
            nextimageButton -> {
                nextImg()
            }
            previousImageButton -> {
                previousImg()
            }
            savecurrent -> {
                saveC()
            }
            resetCurrent -> {
                resetCurrent()
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_photo_editor
    }
}