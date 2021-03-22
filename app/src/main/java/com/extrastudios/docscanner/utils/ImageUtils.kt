package com.extrastudios.docscanner.utils

import android.content.pm.ActivityInfo
import android.graphics.*
import androidx.fragment.app.Fragment
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy

class ImageUtils {

    fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, bmpOriginal.config)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    private object SingletonHolder {
        val INSTANCE = ImageUtils()
    }

    companion object {
        val instance: ImageUtils
            get() = SingletonHolder.INSTANCE

        fun selectImages(frag: Fragment?, requestCode: Int) {
            Matisse.from(frag).choose(MimeType.ofImage(), false).countable(true).capture(false)
                .captureStrategy(CaptureStrategy(true, AUTHORITY_APP)).thumbnailScale(0.85f)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .imageEngine(GlideEngine())
                .maxSelectable(1000) //.imageEngine(new NewPicassoEngine())
                .originalEnable(true).forResult(requestCode)
        }

    }
}