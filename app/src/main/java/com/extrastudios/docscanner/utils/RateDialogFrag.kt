package com.extrastudios.docscanner.utils

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.extrastudios.docscanner.R
import kotlinx.android.synthetic.main.dialog_feedback.*
import kotlinx.android.synthetic.main.dialog_rate.*


open class RateDialogFrag : DialogFragment(), OnRatingBarChangeListener {
    private var callback: RateDialogManager.RateUsCallback? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * 0.82).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private var rating = 0.0f
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_rate, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rb_stars.onRatingBarChangeListener = this

        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        btn_later.setOnClickListener {
            if (rating == 0.0f) {
                Toast.makeText(activity, "Please select 5 star rating!", Toast.LENGTH_SHORT).show()
            } else {
                if (rating >= 4) {
                    val packageName = activity?.packageName
                    var it: Intent
                    try {
                        it = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                        startActivity(it)
                    } catch (anfe: ActivityNotFoundException) {
                        it = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                        startActivity(it)
                    }
                    callback?.onRateSuccess()
                    dismiss()
                } else if (rating > 0) {
                    showRateDialogFeedback(rating)
                    callback?.onFeedback()
                    dismiss()
                }
            }
        }
        bt_never.setOnClickListener {
            callback?.mayBeLater()
            dismiss()
        }
    }

    private fun showRateDialogFeedback(rating: Float) {
        val fm = (activity as AppCompatActivity).supportFragmentManager
        val dialog = RateDialogFeedbackFrag()
        dialog.setRating(rating)
        dialog.show(fm, KEY)
    }

    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        this.rating = rating
    }

    fun setCallback(callback: RateDialogManager.RateUsCallback) {
        this.callback = callback
    }

    companion object {
        const val KEY = "fragment_rate"
    }
}

object RateDialogManager {
    fun showRateDialog(context: Context, callback: RateUsCallback) {
        val fm = (context as AppCompatActivity).supportFragmentManager
        val dialog = RateDialogFrag()
        dialog.setCallback(callback)
        dialog.show(fm, RateDialogFrag.KEY)
    }

    interface RateUsCallback {
        fun onRateSuccess()
        fun onFeedback()
        fun mayBeLater()
    }
}

class RateDialogFeedbackFrag : DialogFragment(), View.OnClickListener {
    private var rating = 0f

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val window = dialog!!.window
        val size = Point()
        val display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        window.setLayout((size.x * 0.82).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_feedback, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        bt_send.setOnClickListener(this)
        bt_no.setOnClickListener(this)
    }

    fun setRating(rating: Float) {
        this.rating = rating
    }

    override fun onClick(view: View) {
        val feedback = et_feedback.text.toString()
        if (view == bt_send && feedback.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.type = "text/plain"
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("extrasstudeios@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " App Rating...!")
            intent.putExtra(Intent.EXTRA_TEXT, "Stars:- $rating\n\nFeedback:- $feedback")
            activity!!.startActivity(Intent.createChooser(intent, "Send email"))
        } else if (view == bt_send) {
            Toast.makeText(activity, "Please enter your feedback!", Toast.LENGTH_SHORT).show()
            return
        }
        dismiss()
    }
}