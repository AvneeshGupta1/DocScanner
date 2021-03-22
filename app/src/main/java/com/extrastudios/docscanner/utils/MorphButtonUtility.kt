package com.extrastudios.docscanner.utils

import android.app.Activity
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import com.dd.morphingbutton.MorphingButton
import com.extrastudios.docscanner.R

class MorphButtonUtility(private val mActivity: Activity) {
    fun integer(): Int {
        return mActivity.resources.getInteger(R.integer.mb_animation)
    }

    private fun dimen(@DimenRes resId: Int): Int {
        return mActivity.resources.getDimension(resId).toInt()
    }

    private fun color(@ColorRes resId: Int): Int {
        return mActivity.resources.getColor(resId)
    }

    fun morphToSquare(btnMorph: MorphingButton, duration: Int) {
        val square = defaultButton(duration)
        val text = if (btnMorph.text.toString()
                .isEmpty()
        ) mActivity.getString(R.string.create_pdf) else btnMorph.text.toString()
        square.color(color(R.color.mb_blue))
        square.colorPressed(color(R.color.mb_blue_dark))
        square.text(text)
        btnMorph.morph(square)
    }

    fun morphToSuccess(btnMorph: MorphingButton) {
        val circle = MorphingButton.Params.create().duration(integer())
            .cornerRadius(dimen(R.dimen.mb_height_56)).width(dimen(R.dimen.mb_height_56))
            .height(dimen(R.dimen.mb_height_56)).color(color(R.color.mb_green))
            .colorPressed(color(R.color.mb_green_dark)).icon(R.drawable.ic_check_white_24dp)
        btnMorph.morph(circle)
    }

    fun morphToGrey(btnMorph: MorphingButton, duration: Int) {
        val square = defaultButton(duration)
        square.color(color(R.color.mb_gray))
        square.colorPressed(color(R.color.mb_gray))
        square.text(btnMorph.text.toString())
        btnMorph.morph(square)
    }

    fun morphToWhite(btnMorph: MorphingButton, duration: Int) {
        val square = defaultButton(duration)
        square.color(color(R.color.mb_gray))
        square.colorPressed(color(R.color.white))
        square.text(btnMorph.text.toString())
        btnMorph.morph(square)
    }

    private fun defaultButton(duration: Int): MorphingButton.Params {
        return MorphingButton.Params.create().duration(duration)
            .cornerRadius(dimen(R.dimen.mb_corner_radius_2))
            .width(FrameLayout.LayoutParams.MATCH_PARENT)
            .height(FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    fun setTextAndActivateButtons(
        path: String?,
        toSetPathOn: MorphingButton,
        toEnable: MorphingButton
    ) {
        toSetPathOn.text = path
        toSetPathOn.setBackgroundColor(mActivity.resources.getColor(R.color.mb_green_dark))
        toEnable.isEnabled = true
        morphToSquare(toEnable, integer())
    }

    fun initializeButton(button: MorphingButton, buttonToDisable: MorphingButton) {
        button.setText(R.string.merge_file_select)
        button.setBackgroundColor(mActivity.resources.getColor(R.color.mb_blue))
        morphToGrey(buttonToDisable, integer())
        buttonToDisable.isEnabled = false
    }

    fun initializeButtonForAddText(
        pdfButton: MorphingButton,
        textButton: MorphingButton,
        buttonToDisable: MorphingButton
    ) {
        pdfButton.setText(R.string.select_pdf_file)
        pdfButton.setBackgroundColor(mActivity.resources.getColor(R.color.mb_blue))
        textButton.setText(R.string.select_text_file)
        textButton.setBackgroundColor(mActivity.resources.getColor(R.color.mb_blue))
        morphToGrey(buttonToDisable, integer())
        buttonToDisable.isEnabled = false
    }

}