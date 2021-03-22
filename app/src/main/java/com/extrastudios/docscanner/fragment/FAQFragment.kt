package com.extrastudios.docscanner.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.FAQAdapter
import com.extrastudios.docscanner.model.FAQItem
import kotlinx.android.synthetic.main.fragment_faq.*
import java.util.*
import kotlin.collections.ArrayList

class FAQFragment : Fragment() {
    private var mFaqAdapter: FAQAdapter? = null
    private var mFaqs = ArrayList<FAQItem>()
    private var mFaqsCopy = ArrayList<FAQItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterFaq(query)
                return true
            }

            override fun onQueryTextChange(newQuery: String): Boolean {
                filterFaq(newQuery)
                return true
            }
        })
        initFAQs()
        initFAQRecyclerView()
    }

    fun filterFaq(searchText: String) {
        var text = searchText
        mFaqs.clear()
        if (text.isEmpty()) mFaqs.addAll(mFaqsCopy) else {
            text = text.toLowerCase(Locale.ROOT)
            for (faq in mFaqsCopy) {
                if (faq.question.toLowerCase(Locale.ROOT).contains(text)) {
                    mFaqs.add(faq)
                }
            }
        }
        mFaqAdapter?.notifyDataSetChanged()
    }

    private fun initFAQs() {
        val questionAnswers = resources.getStringArray(R.array.faq_question_answers)
        var faqItem: FAQItem
        for (questionAnswer in questionAnswers) {
            val questionAnswerSplit = questionAnswer.split("#####").toTypedArray()
            faqItem = FAQItem(questionAnswerSplit[0], questionAnswerSplit[1])
            mFaqs.add(faqItem)
        }
        mFaqsCopy.addAll(mFaqs)
    }

    private fun initFAQRecyclerView() {
        mFaqAdapter = FAQAdapter(mFaqs) { index -> onItemClick(index) }
        recycler_view_faq!!.adapter = mFaqAdapter
    }

    fun onItemClick(position: Int) {
        val faqItem = mFaqs[position]
        faqItem.isExpanded = !faqItem.isExpanded
        mFaqAdapter!!.notifyItemChanged(position)
    }
}