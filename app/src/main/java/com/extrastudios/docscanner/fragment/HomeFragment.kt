package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.utils.TAKE_PHOTO
import com.extrastudios.docscanner.viewmodel.HomeViewModel
import com.vlk.multimager.activities.MultiCameraActivity
import com.vlk.multimager.utils.Constants
import com.vlk.multimager.utils.Constants.KEY_PARAMS
import com.vlk.multimager.utils.Image
import com.vlk.multimager.utils.Params
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File

class HomeFragment : BaseFragments() {

    private val photoCode = 32
    private val homeItemList = ArrayList<CommonItem>()
    private var homeAdapter: HomeAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeRecyclerView
    }

    private val homeRecyclerView by lazy {
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity, 6)
        recyclerView.layoutManager = layoutManager
        homeAdapter = HomeAdapter(homeItemList) { item -> onItemClick(item) }
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                homeAdapter?.let {
                    if (it.isHeader(position)) return 6
                    if (position == 1) return 6
                    if (it.isShow3Item(position)) return 2
                    return 3
                }
                return 2
            }
        }

        recyclerView.adapter = homeAdapter
        loadData()
    }

    private fun loadData() {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.getHomeItems(this).observe(this, Observer {
            homeItemList.clear()
            homeItemList.addAll(it)
            homeAdapter?.notifyDataSetChanged()
        })
    }

    private fun initiateMultiCapture() {
        val intent = Intent(activity, MultiCameraActivity::class.java)
        val params = Params()
        params.captureLimit = 10
        intent.putExtra(KEY_PARAMS, params)
        startActivityForResult(intent, photoCode)
    }

    private fun onItemClick(type: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        if (type == TAKE_PHOTO) {
            checkStorageAndCameraPermission()
        } else (activity as HomeActivity).onItemSelected(type)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == photoCode && data != null) {
            val imagesList: ArrayList<Image> =
                data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST)!!
            if (imagesList.size != 0) {
                try {

                    val imageUris = ArrayList<Uri>()
                    for (i in imagesList.indices) {
                        imageUris.add(Uri.fromFile(File(imagesList[i].imagePath)))
                    }
                    (activity as HomeActivity).convertImagesToPdf(imageUris)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStoragePermissionAllow() {
        initiateMultiCapture()
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {

    }


}