package com.example.test.ui.activity

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
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

        refresh.setOnRefreshListener {
            viewModel.reloadData { offers, banners ->
                subscribe(offers, banners)
            }
        }

        tvOfferError.visibility = View.GONE
        tvBannerError.visibility = View.GONE
        rvOffers.visibility = View.GONE
        vpBanners.visibility = View.GONE

        subscribe(viewModel.offersObservable, viewModel.bannersObservable)
    }

    private fun subscribe(
            offers: ConnectableObservable<Model.Result<List<OffersAdapter.Item>>>,
            banners: ConnectableObservable<Model.Result<List<BannersAdapter.BannerItem>>>
    ) {
        showProgress()
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
        private var bannersDisposable: Disposable? = null
        lateinit var bannersObservable: ConnectableObservable<Model.Result<List<BannersAdapter.BannerItem>>>

        @Inject
        lateinit var model: Model

        init {
            App.appComponent.inject(this)
            reloadData()
        }

        override fun onCleared() {
            super.onCleared()
            offersDisposable?.dispose()
            bannersDisposable?.dispose()
        }

        fun reloadData(
                func: (
                        offers: ConnectableObservable<Model.Result<List<OffersAdapter.Item>>>,
                        banners: ConnectableObservable<Model.Result<List<BannersAdapter.BannerItem>>>
                ) -> Unit
        ) {
            reloadData()
            func(offersObservable, bannersObservable)
        }

        private fun reloadData() {
            offersDisposable?.dispose()
            offersObservable = model.getOffers()
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

            bannersDisposable?.dispose()
            bannersObservable = model.getBanners()
                    .map { Model.Result(it.isFromCache, it.data.map { BannersAdapter.BannerItem(it) }) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .replay()
            bannersDisposable = bannersObservable.connect()
        }
    }
}
