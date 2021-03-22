package com.extrastudios.docscanner.utils

import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class BottomSheetCallback(private val mUpArrow: ImageView, private val mIsAdded: Boolean) :
    BottomSheetBehavior.BottomSheetCallback() {
    override fun onStateChanged(bottomSheet: View, newState: Int) {}
    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (mIsAdded) {
            animateBottomSheetArrow(slideOffset)
        }
    }

    private fun animateBottomSheetArrow(slideOffset: Float) {
        if (slideOffset in 0.0..1.0) {
            mUpArrow.rotation = slideOffset * -180
        } else if (slideOffset >= -1 && slideOffset < 0) {
            mUpArrow.rotation = slideOffset * 180
        }
    }

}