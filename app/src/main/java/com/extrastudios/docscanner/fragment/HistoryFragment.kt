package com.extrastudios.docscanner.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.HistoryAdapter
import com.extrastudios.docscanner.database.entity.History
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import org.jetbrains.anko.alert
import java.io.File
import java.lang.Boolean.TRUE
import java.util.*
import kotlin.collections.ArrayList

class HistoryFragment : BaseFragments(), View.OnClickListener {

    private var menuItemDelete: MenuItem? = null
    private var menuItemFilter: MenuItem? = null
    private var mFilterOptionState: BooleanArray? = null
    private var historyAdapter: HistoryAdapter? = null
    private val historyList = ArrayList<History>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFilterOptionState =
            BooleanArray(resources.getStringArray(R.array.filter_options_history).size)
        Arrays.fill(mFilterOptionState!!, TRUE)
        getStarted.setOnClickListener(this)
        mRecyclerView
        btnPermission.setOnClickListener(this)
        checkStoragePermission()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history_fragment, menu)
        menuItemDelete = menu.findItem(R.id.actionDeleteHistory)
        menuItemFilter = menu.findItem(R.id.actionFilterHistory)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionDeleteHistory -> showDeleteConfirmation()
            R.id.actionFilterHistory -> openFilterDialog()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showDeleteConfirmation() {
        activity?.alert(R.string.delete_history_message, R.string.warning) {
            positiveButton(android.R.string.ok) {
                deleteHistory()
            }
            negativeButton(android.R.string.cancel)
        }?.show()
    }

    private fun deleteHistory() {
        val viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        viewModel.deleteHistory().observe(this, Observer {
            if (it) {
                loadHistory()
            }
        })
    }

    private fun openFilterDialog() {
        val builder = AlertDialog.Builder(activity)

        val options = resources.getStringArray(R.array.filter_options_history)

        builder.setMultiChoiceItems(
            options,
            mFilterOptionState
        ) { _: DialogInterface?, index: Int, isChecked: Boolean ->
            mFilterOptionState!![index] = isChecked
        }

        builder.setTitle(getString(R.string.title_filter_history_dialog))

        builder.setPositiveButton(R.string.ok) { _, i ->
            loadHistory()
        }

        builder.setNeutralButton(getString(R.string.select_all)) { dialogInterface: DialogInterface?, i: Int ->
            Arrays.fill(mFilterOptionState!!, TRUE)
            loadHistory()
        }
        builder.create().show()
    }

    private val mRecyclerView by lazy {
        historyRecyclerView.itemAnimator = DefaultItemAnimator()
        historyRecyclerView.setHasFixedSize(true)
        historyRecyclerView.setDivider()
        historyAdapter = HistoryAdapter(historyList) { item -> onHistoryClick(item) }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun loadHistory() {
        progressBar.show()
        val selectedOptions = ArrayList<Int>()
        for (j in mFilterOptionState!!.indices) {
            if (mFilterOptionState!![j]) {
                selectedOptions.add(j)
            }
        }
        val excelToPDFViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        excelToPDFViewModel.getHistory(selectedOptions).observe(this, Observer {
            progressBar.hide()
            historyList.clear()
            historyList.addAll(it)
            historyAdapter?.notifyDataSetChanged()
            emptyStatusView.visibleIf(historyList.isEmpty())
            historyRecyclerView.visibleIf(historyList.isNotEmpty())
            menuItemDelete?.isVisible = historyList.isNotEmpty()
        })
    }

    private fun onHistoryClick(history: History) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        val file = File(history.file_path)
        if (file.exists()) {
            mFileUtils?.openFile(history.file_path, FileUtils.FileType.e_PDF)
        } else {
            showSnackbar(R.string.pdf_does_not_exist_message)
        }
    }

    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadHistory()
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }

    override fun onClick(v: View?) {
        when (v) {
            getStarted -> {
                (activity as HomeActivity).onItemSelected(R.id.nav_home)
            }
            btnPermission -> checkStoragePermission()
        }
    }
}