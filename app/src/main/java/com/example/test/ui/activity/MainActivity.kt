package com.example.test.ui.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.example.test.App
import com.example.test.R
import com.example.test.model.Model
import com.example.test.ui.adapter.BannersAdapter
import com.example.test.ui.adapter.FiltersAdapter
import com.example.test.ui.adapter.OffersAdapter
import com.example.test.utils.handleError
import floatingsearchviewrxbinding.RxFloatingSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : FragmentActivity() {
    private lateinit var viewModel: MainActivityViewModel

    private var offersDisposable: Disposable? = null
    private val offersAdapter = OffersAdapter({
        Toast.makeText(this@MainActivity, "Offer $it clicked", Toast.LENGTH_SHORT).show()
    }, {
        Toast.makeText(this@MainActivity, "Offer remove $it clicked", Toast.LENGTH_SHORT).show()
    })

    private var bannersDisposable: Disposable? = null
    private val bannersAdapter = BannersAdapter()

    private var searchQueryDisposable: Disposable? = null
    private var suggestionsDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        rvFilters.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFilters.adapter = FiltersAdapter()
                .also {
                    it.data = listOf(
                            FiltersAdapter.FilterItem("Топ-10", false),
                            FiltersAdapter.FilterItem("Магазины", false),
                            FiltersAdapter.FilterItem("Товары", false)
                    )
                }
        rvFilters.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.left = (resources.displayMetrics.density * 17).toInt()
                    outRect.right = (resources.displayMetrics.density * 17).toInt()
                    outRect.top = (resources.displayMetrics.density * 17).toInt()
                    outRect.bottom = (resources.displayMetrics.density * 17).toInt()
                } else {
                    outRect.left = 0
                    outRect.right = (resources.displayMetrics.density * 17).toInt()
                    outRect.top = (resources.displayMetrics.density * 17).toInt()
                    outRect.bottom = (resources.displayMetrics.density * 17).toInt()
                }
            }
        })

        rvOffers.layoutManager = LinearLayoutManager(this)
        rvOffers.adapter = offersAdapter

        vpBanners.pageMargin = (resources.displayMetrics.density * -107).toInt()
        vpBanners.adapter = bannersAdapter

        btnInfo.setOnClickListener {
            AlertDialog.Builder(this@MainActivity)
                    .setPositiveButton(R.string.btn_ok) { dialog, _ -> dialog.dismiss() }
                    .setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
                    .setOnCancelListener { dialog -> dialog.dismiss() }
                    .setCancelable(true)
                    .show()
        }

        initSearchView()

        refresh.setOnRefreshListener {
            showProgress()
            subscribeOffers(viewModel.reloadOffers())
            subscribeBanners(viewModel.reloadBanners())
        }

        tvOfferError.visibility = View.GONE
        tvBannerError.visibility = View.GONE
        rvOffers.visibility = View.GONE
        vpBanners.visibility = View.GONE

        showProgress()
        subscribeOffers(viewModel.offersObservable)
        subscribeBanners(viewModel.bannersObservable)
        viewModel.suggestionObservable?.let { subscribeSuggestions(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        offersDisposable?.dispose()
        bannersDisposable?.dispose()
        suggestionsDisposable?.dispose()
    }

    override fun onBackPressed() {
        if (search.isSearchBarFocused) {
            search.setSearchFocused(false)
        } else {
            super.onBackPressed()
        }
    }

    private fun initSearchView() {
        search.setSearchText(viewModel.searchQuery ?: "")

        searchQueryDisposable = RxFloatingSearchView.queryChanges(search, 0)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { query ->
                    if (query.isBlank() || !search.isSearchBarFocused) {
                        viewModel.searchQuery = ""
                        search.clearSuggestions()
                        return@subscribe
                    }
                    viewModel.searchQuery = query.toString()
                    search.showProgress()
                    subscribeSuggestions(viewModel.reloadSuggestions(query.toString()))
                }

        search.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSearchAction(currentQuery: String) {
                showProgress()
                subscribeOffers(viewModel.reloadOffers())
            }

            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                searchSuggestion as MainActivityViewModel.OfferSuggestion
                Toast.makeText(this@MainActivity, "Offer ${searchSuggestion.id} clicked", Toast.LENGTH_SHORT).show()
            }
        })

        search.setOnFocusChangeListener(object : FloatingSearchView.OnFocusChangeListener {
            override fun onFocusCleared() {
                viewModel.cancelSuggestions()
                search.hideProgress()
                suggestionsDisposable?.dispose()
            }

            override fun onFocus() {
            }
        })
    }

    private fun subscribeOffers(offers: ConnectableObservable<Model.Result<List<OffersAdapter.Item>>>) {
        offersDisposable?.dispose()
        offersDisposable = offers
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isFromCache) {
                        Snackbar.make(root, R.string.err_from_cache, Snackbar.LENGTH_SHORT).show()
                    }
                    showOffers(it.data)
                }, {
                    handleError(it) { msg -> showOfferError(msg) }
                    it.printStackTrace()
                })
    }

    private fun subscribeBanners(banners: ConnectableObservable<Model.Result<List<BannersAdapter.BannerItem>>>) {
        bannersDisposable?.dispose()
        bannersDisposable = banners
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isFromCache) {
                        Snackbar.make(root, R.string.err_from_cache, Snackbar.LENGTH_SHORT).show()
                    }
                    showBanners(it.data)
                }, {
                    handleError(it) { msg -> showBannerError(msg) }
                    it.printStackTrace()
                })
    }

    private fun subscribeSuggestions(offers: ConnectableObservable<List<MainActivityViewModel.OfferSuggestion>>) {
        suggestionsDisposable?.dispose()
        suggestionsDisposable = offers
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    search.swapSuggestions(it)
                    search.hideProgress()
                }, {
                    it.printStackTrace()
                    search.hideProgress()
                })
    }

    private fun showBannerError(msg: String) {
        refresh.isRefreshing = false
        tvBannerError.text = msg
        tvBannerError.visibility = View.VISIBLE
        vpBanners.visibility = View.GONE
    }

    private fun showOfferError(msg: String) {
        refresh.isRefreshing = false
        tvOfferError.text = msg
        tvOfferError.visibility = View.VISIBLE
        rvOffers.visibility = View.GONE
    }

    private fun showBanners(banners: List<BannersAdapter.BannerItem>) {
        refresh.isRefreshing = false
        tvBannerError.visibility = View.GONE
        vpBanners.visibility = View.VISIBLE
        bannersAdapter.data = banners
    }

    private fun showOffers(offers: List<OffersAdapter.Item>) {
        refresh.isRefreshing = false
        tvOfferError.visibility = View.GONE
        rvOffers.visibility = View.VISIBLE
        offersAdapter.data = offers
    }

    private fun showProgress() {
        refresh.isRefreshing = true
    }

    class MainActivityViewModel : ViewModel() {
        private var offersDisposable: Disposable? = null
        lateinit var offersObservable: ConnectableObservable<Model.Result<List<OffersAdapter.Item>>>
            private set

        private var bannersDisposable: Disposable? = null
        lateinit var bannersObservable: ConnectableObservable<Model.Result<List<BannersAdapter.BannerItem>>>
            private set

        var searchQuery: String? = null
        private var suggestionDisposable: Disposable? = null
        var suggestionObservable: ConnectableObservable<List<OfferSuggestion>>? = null
            private set

        @Inject
        lateinit var model: Model

        @SuppressLint("StaticFieldLeak")
        @Inject
        lateinit var context: Context

        init {
            App.appComponent.inject(this)
            reloadOffers()
            reloadBanners()
        }

        override fun onCleared() {
            super.onCleared()
            offersDisposable?.dispose()
            bannersDisposable?.dispose()
            suggestionDisposable?.dispose()
        }

        fun reloadOffers(): ConnectableObservable<Model.Result<List<OffersAdapter.Item>>> {
            offersDisposable?.dispose()
            offersObservable = model.getOffers(searchQuery)
                    .map { Model.Result(it.isFromCache, it.data.groupBy { it.groupName }) }
                    .map {
                        val result: MutableList<OffersAdapter.Item> = ArrayList()
                        it.data.forEach {
                            result += OffersAdapter.GroupHeaderItem(it.key)
                            result += it.value.map { OffersAdapter.OfferItem(it) }
                        }
                        Model.Result(it.isFromCache, result as List<OffersAdapter.Item>)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .replay()
            offersDisposable = offersObservable.connect()
            return offersObservable
        }

        fun reloadBanners(): ConnectableObservable<Model.Result<List<BannersAdapter.BannerItem>>> {
            bannersDisposable?.dispose()
            bannersObservable = model.getBanners()
                    .map { Model.Result(it.isFromCache, it.data.map { BannersAdapter.BannerItem(it) }) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .replay()
            bannersDisposable = bannersObservable.connect()
            return bannersObservable
        }

        fun reloadSuggestions(query: String): ConnectableObservable<List<OfferSuggestion>> {
            suggestionDisposable?.dispose()
            return model.getOffers(query)
                    .toObservable()
                    .map { it.data.take(4) }
                    .map { it.map { OfferSuggestion(it.id, it.title ?: context.getString(R.string.no_title)) } }
                    .replay()
                    .also { suggestionObservable = it }
                    .also { suggestionDisposable = it.connect() }
        }

        fun cancelSuggestions() {
            suggestionDisposable?.dispose()
            suggestionObservable = null
        }

        class OfferSuggestion(
                val id: String,
                val title: String
        ) : SearchSuggestion {
            override fun getBody(): String = title

            constructor(parcel: Parcel) : this(
                    parcel.readString(),
                    parcel.readString())

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(id)
                parcel.writeString(title)
            }

            override fun describeContents(): Int {
                return 0
            }

            companion object CREATOR : Parcelable.Creator<OfferSuggestion> {
                override fun createFromParcel(parcel: Parcel): OfferSuggestion {
                    return OfferSuggestion(parcel)
                }

                override fun newArray(size: Int): Array<OfferSuggestion?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
