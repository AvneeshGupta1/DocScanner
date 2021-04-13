package com.extrastudios.docscanner.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.eftimoff.viewpagertransformers.DepthPageTransformer
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.PreviewAdapter
import com.extrastudios.docscanner.utils.PREVIEW_IMAGES
import java.util.*

class ImagesPreviewActivity : BaseActivity() {

    companion object {
        fun getStartIntent(context: Context?, uris: ArrayList<String>?): Intent {
            val intent = Intent(context, ImagesPreviewActivity::class.java)
            intent.putExtra(PREVIEW_IMAGES, uris)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val mImagesArrayList = intent.getStringArrayListExtra(PREVIEW_IMAGES)?:ArrayList()
        val mViewPager = findViewById<ViewPager>(R.id.viewpager)
        val mPreviewAdapter = PreviewAdapter(this, mImagesArrayList)
        mViewPager.adapter = mPreviewAdapter
        mViewPager.setPageTransformer(true, DepthPageTransformer())
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_preview_images
    }
}