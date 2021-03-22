package com.extrastudios.docscanner.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.RearrangePdfAdapter
import com.extrastudios.docscanner.utils.RESULT
import com.extrastudios.docscanner.utils.SAME_FILE
import com.extrastudios.docscanner.utils.hide
import kotlinx.android.synthetic.main.activity_rearrange_images.*
import java.util.*

class RearrangePdfPages : BaseActivity(), RearrangePdfAdapter.OnClickListener {

    private var mRearrangeImagesAdapter: RearrangePdfAdapter? = null
    private var mSequence: ArrayList<Int>? = null
    private var mInitialSequence: ArrayList<Int>? = null

    companion object {
        var mImages: ArrayList<Bitmap>? = null
        fun getStartIntent(context: Context): Intent {
            return Intent(context, RearrangePdfPages::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        mSequence = ArrayList()
        mInitialSequence = ArrayList()
        sort.hide()
        if (mImages == null || mImages!!.size < 1) {
            finish()
        } else initRecyclerView(mImages)
    }


    private fun initRecyclerView(images: ArrayList<Bitmap>?) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        mRearrangeImagesAdapter = RearrangePdfAdapter(this, images!!)
        recyclerView.adapter = mRearrangeImagesAdapter
        mSequence = ArrayList()
        for (i in images.indices) {
            mSequence!!.add(i + 1)
        }
        mInitialSequence!!.addAll(mSequence!!)
    }

    private fun swap(pos1: Int, pos2: Int) {
        if (pos1 >= mSequence!!.size) return
        val `val` = mSequence!![pos1]
        mSequence!![pos1] = mSequence!![pos2]
        mSequence!![pos2] = `val`
    }

    override fun onUpClick(position: Int) {
        mImages!!.add(position - 1, mImages!!.removeAt(position))
        mRearrangeImagesAdapter!!.positionChanged(mImages!!)
        swap(position, position - 1)
    }

    override fun onDownClick(position: Int) {
        mImages!!.add(position + 1, mImages!!.removeAt(position))
        mRearrangeImagesAdapter!!.positionChanged(mImages!!)
        swap(position, position + 1)
    }

    override fun onRemoveClick(position: Int) {
        if (preferencesService.removePage) {
            mImages!!.removeAt(position)
            mRearrangeImagesAdapter!!.positionChanged(mImages!!)
            mSequence!!.removeAt(position)
        } else {
            val builder: MaterialDialog.Builder =
                MaterialDialog.Builder(this).title(R.string.warning)
                    .content(R.string.remove_page_message).positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
            builder.checkBoxPrompt(getString(R.string.dont_show_again), false, null)
                .onPositive { dialog: MaterialDialog, which: DialogAction? ->
                    if (dialog.isPromptCheckBoxChecked) {
                        preferencesService.removePage = true
                    }
                    mImages!!.removeAt(position)
                    mRearrangeImagesAdapter!!.positionChanged(mImages!!)
                    mSequence!!.removeAt(position)
                }.show()
        }
    }

    private fun passUris() {
        val returnIntent = Intent()
        val result = StringBuilder()
        for (x in mSequence!!) result.append(x).append(",")
        returnIntent.putExtra(RESULT, result.toString())
        val sameFile = mInitialSequence == mSequence
        returnIntent.putExtra(SAME_FILE, sameFile)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onBackPressed() {
        passUris()
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            passUris()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getLayout(): Int {
        return R.layout.activity_rearrange_images
    }
}