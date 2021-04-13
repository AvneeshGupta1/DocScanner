package com.extrastudios.docscanner.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.eftimoff.viewpagertransformers.DepthPageTransformer
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.PreviewAdapter
import com.extrastudios.docscanner.adapter.PreviewImageOptionsAdapter
import com.extrastudios.docscanner.model.PreviewImageOptionItem
import com.extrastudios.docscanner.utils.ImageSortUtils
import com.extrastudios.docscanner.utils.PREVIEW_IMAGES
import com.extrastudios.docscanner.utils.RESULT
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : BaseActivity() {
    private var mImagesArrayList: ArrayList<String> = ArrayList()
    private var mPreviewAdapter: PreviewAdapter? = null
    private val rearrangeImageCode = 1

    companion object {
        fun getStartIntent(context: Context?, uris: ArrayList<String>): Intent {
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putExtra(PREVIEW_IMAGES, uris)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mImagesArrayList = intent.getStringArrayListExtra(PREVIEW_IMAGES)?:ArrayList()
        mPreviewAdapter = PreviewAdapter(this, mImagesArrayList)
        viewPager?.adapter = mPreviewAdapter
        viewPager?.setPageTransformer(true, DepthPageTransformer())
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        showOptions()
    }

    private fun showOptions() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.layoutManager = layoutManager
        val adapter = PreviewImageOptionsAdapter(options) { index -> onItemClick(index) }
        recyclerView?.adapter = adapter
    }

    private val options: ArrayList<PreviewImageOptionItem>
        get() {
            val mOptions = ArrayList<PreviewImageOptionItem>()
            mOptions.add(
                PreviewImageOptionItem(
                    R.drawable.ic_rearrange,
                    getString(R.string.rearrange_text)
                )
            )
            mOptions.add(PreviewImageOptionItem(R.drawable.ic_sort, getString(R.string.sort)))
            return mOptions
        }

    private fun onItemClick(position: Int) {
        when (position) {
            0 -> startActivityForResult(
                RearrangeImages.getStartIntent(this, mImagesArrayList),
                rearrangeImageCode
            )
            1 -> sortImages()
        }
    }

    private fun sortImages() {
        MaterialDialog.Builder(this).title(R.string.sort_by_title)
            .items(R.array.sort_options_images)
            .itemsCallback { dialog: MaterialDialog?, itemView: View?, position: Int, text: CharSequence? ->
                ImageSortUtils.instance.performSortOperation(position, mImagesArrayList)
                mPreviewAdapter!!.setData(ArrayList(mImagesArrayList))
                viewPager!!.adapter = mPreviewAdapter
            }.negativeText(R.string.cancel).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        if (requestCode == rearrangeImageCode) {
            try {
                mImagesArrayList = data.getStringArrayListExtra(RESULT)?:ArrayList()
                mPreviewAdapter!!.setData(mImagesArrayList)
                viewPager!!.adapter = mPreviewAdapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putStringArrayListExtra(RESULT, mImagesArrayList)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun getLayout(): Int {
        return R.layout.activity_preview
    }

}