package com.cyberveda.client.ui.main.feedback

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.cyberveda.client.R
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.FEEDBACK_FILTER_DATE_UPDATED
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.FEEDBACK_FILTER_USERNAME
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.FEEDBACK_ORDER_ASC
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.FEEDBACK_ORDER_DESC
import com.cyberveda.client.ui.DataState
import com.cyberveda.client.ui.main.feedback.state.FeedbackViewState
import com.cyberveda.client.ui.main.feedback.viewmodel.*
import com.cyberveda.client.util.ErrorHandling
import com.cyberveda.client.util.TopSpacingItemDecoration
import handleIncomingFeedbackListData
import kotlinx.android.synthetic.main.fragment_feedback.*
import loadFirstPage
import nextPage
import refreshFromCache

class FeedbackFragment : BaseFeedbackFragment(),
    FeedbackListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener
{

    private lateinit var searchView: SearchView
    private lateinit var recyclerAdapter: FeedbackListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFromCache()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun saveLayoutManagerState(){
        feedback_post_recyclerview.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            if(dataState != null) {
                // call before onDataStateChange to consume error if there is one
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
            Log.d(TAG, "FeedbackFragment, ViewState: ${viewState}")
            if(viewState != null){
                recyclerAdapter.apply {
                    preloadGlideImages(
                        requestManager = dependencyProvider.getGlideRequestManager(),
                        list = viewState.feedbackFields.feedbackList
                    )
                    submitList(
                        feedbackList = viewState.feedbackFields.feedbackList,
                        isQueryExhausted = viewState.feedbackFields.isQueryExhausted
                    )
                }

            }
        })
    }

    private fun initSearchView(menu: Menu){
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH ) {
                val searchQuery = v.text.toString()
                Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: ${searchQuery}")
                viewModel.setQuery(searchQuery).let{
                    onFeedbackSearchOrFilter()
                }
            }
            true
        }

        // SEARCH BUTTON CLICKED (in toolbar)
        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView: (button) executing search...: ${searchQuery}")
            viewModel.setQuery(searchQuery).let {
                onFeedbackSearchOrFilter()
            }

        }
    }

    private fun onFeedbackSearchOrFilter(){
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private  fun resetUI(){
        feedback_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    private fun handlePagination(dataState: DataState<FeedbackViewState>){

        // Handle incoming data from DataState
        dataState.data?.let {
            it.data?.let{
                it.getContentIfNotHandled()?.let{
                    viewModel.handleIncomingFeedbackListData(it)
                }
            }
        }

        // Check for pagination end (no more results)
        // must do this b/c server will return an ApiErrorResponse if page is not valid,
        // -> meaning there is no more data.
        dataState.error?.let{ event ->
            event.peekContent().response.message?.let{
                if(ErrorHandling.isPaginationDone(it)){

                    // handle the error message event so it doesn't display in UI
                    event.getContentIfNotHandled()

                    // set query exhausted to update RecyclerView with
                    // "No more results..." list item
                    viewModel.setQueryExhausted(true)
                }
            }
        }
    }

    private fun initRecyclerView(){

        feedback_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@FeedbackFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = FeedbackListAdapter(
                dependencyProvider.getGlideRequestManager(),
                this@FeedbackFragment
            )
            addOnScrollListener(object: RecyclerView.OnScrollListener(){

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "FeedbackFragment: attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_filter_settings -> {
                showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(position: Int, item: FeedbackPost) {
        viewModel.setFeedbackPost(item)
        findNavController().navigate(R.id.action_feedbackFragment_to_viewFeedbackFragment)
    }

    override fun restoreListPosition() {
        viewModel.viewState.value?.feedbackFields?.layoutManagerState?.let { lmState ->
            feedback_post_recyclerview?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear references (can leak memory)
        feedback_post_recyclerview.adapter = null
    }

    override fun onRefresh() {
        onFeedbackSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }

    fun showFilterDialog(){

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_feedback_filter)

            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            view.findViewById<RadioGroup>(R.id.filter_group).apply {
                when (filter) {
                    FEEDBACK_FILTER_DATE_UPDATED -> check(R.id.filter_date)
                    FEEDBACK_FILTER_USERNAME -> check(R.id.filter_author)
                }
            }

            view.findViewById<RadioGroup>(R.id.order_group).apply {
                when (order) {
                    FEEDBACK_ORDER_ASC -> check(R.id.filter_asc)
                    FEEDBACK_ORDER_DESC -> check(R.id.filter_desc)
                }
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: apply filter.")

                val newFilter =
                    when (view.findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId) {
                        R.id.filter_author -> FEEDBACK_FILTER_USERNAME
                        R.id.filter_date -> FEEDBACK_FILTER_DATE_UPDATED
                        else -> FEEDBACK_FILTER_DATE_UPDATED
                    }

                val newOrder =
                    when (view.findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId) {
                        R.id.filter_desc -> "-"
                        else -> ""
                    }

                viewModel.apply {
                    saveFilterOptions(newFilter, newOrder)
                    setFeedbackFilter(newFilter)
                    setFeedbackOrder(newOrder)
                }

                onFeedbackSearchOrFilter()

                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: cancelling filter.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}

















