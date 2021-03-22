package com.extrastudios.docscanner.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.RearrangeImagesAdapter
import com.extrastudios.docscanner.utils.ImageSortUtils
import com.extrastudios.docscanner.utils.PREVIEW_IMAGES
import com.extrastudios.docscanner.utils.RESULT
import kotlinx.android.synthetic.main.activity_rearrange_images.*
import java.util.*

class RearrangeImages : BaseActivity(), RearrangeImagesAdapter.OnClickListener,
    View.OnClickListener {

    private var mImages: ArrayList<String>? = null
    private var mRearrangeImagesAdapter: RearrangeImagesAdapter? = null

    companion object {
        fun getStartIntent(context: Context?, uris: ArrayList<String>): Intent {
            val intent = Intent(context, RearrangeImages::class.java)
            intent.putExtra(PREVIEW_IMAGES, uris)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sort.setOnClickListener(this)
        mImages = intent.getStringArrayListExtra(PREVIEW_IMAGES)
        initRecyclerView(mImages)
    }

    private fun initRecyclerView(images: ArrayList<String>?) {
        mRearrangeImagesAdapter = RearrangeImagesAdapter(this, images!!)
        recyclerView.adapter = mRearrangeImagesAdapter
    }

    override fun onUpClick(position: Int) {
        mImages!!.add(position - 1, mImages!!.removeAt(position))
        mRearrangeImagesAdapter?.positionChanged(mImages!!)
    }

    override fun onDownClick(position: Int) {
        mImages!!.add(position + 1, mImages!!.removeAt(position))
        mRearrangeImagesAdapter?.positionChanged(mImages!!)
    }

    override fun onRemoveClick(position: Int) {
        if (preferencesService.confirmRemove) {
            mImages?.removeAt(position)
            mRearrangeImagesAdapter?.positionChanged(mImages!!)
        } else {
            val builder = MaterialDialog.Builder(this).title(R.string.warning)
                .content(R.string.remove_image_message).positiveText(R.string.ok)
                .negativeText(R.string.cancel)
            builder.checkBoxPrompt(getString(R.string.dont_show_again), false, null)
                .onPositive { dialog: MaterialDialog, which: DialogAction? ->
                    preferencesService.confirmRemove = dialog.isPromptCheckBoxChecked
                    mImages!!.removeAt(position)
                    mRearrangeImagesAdapter!!.positionChanged(mImages!!)
                }.show()
        }
    }

    private fun passUris() {
        val returnIntent = Intent()
        returnIntent.putStringArrayListExtra(RESULT, mImages)
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

    private fun sortImages() {
        MaterialDialog.Builder(this).title(R.string.sort_by_title)
            .items(R.array.sort_options_images)
            .itemsCallback { dialog: MaterialDialog?, itemView: View?, position: Int, text: CharSequence? ->
                ImageSortUtils.instance.performSortOperation(position, mImages!!)
                mRearrangeImagesAdapter!!.positionChanged(mImages!!)
            }.negativeText(R.string.cancel).show()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.sort) {
            sortImages()
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_rearrange_images
    }
}