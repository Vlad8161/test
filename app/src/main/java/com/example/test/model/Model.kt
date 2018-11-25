package com.example.test.model

import android.util.Log
import com.example.test.model.data.Banner
import com.example.test.model.data.Offer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Created by vlad on 24.11.18.
 */

class Model(
        private val storage: Storage,
        private val serverApi: ServerApi
) {

    fun getOffers(query: String? = null): Single<Result<List<Offer>>> = serverApi.getOffers()
            //.delay(5, TimeUnit.SECONDS)
            //.doOnSuccess { throw RuntimeException() }
            .map { if (query != null && !query.isBlank()) filterOffers(it, query) else it }
            .subscribeOn(Schedulers.io())
            .let { storage.updateOffers(it) }
            .observeOn(Schedulers.io())
            .map { Result(false, it) }
            .onErrorResumeNext { networkError ->
                storage.getOffers()
                        .doOnSuccess { if (it.isEmpty()) throw networkError }
                        .map { if (!query.isNullOrBlank()) filterOffers(it, query!!) else it }
                        .map { Result(true, it) }
            }

    fun getBanners(): Single<Result<List<Banner>>> = serverApi.getBanners()
            //.delay(5, TimeUnit.SECONDS)
            //.doOnSuccess { throw RuntimeException() }
            .subscribeOn(Schedulers.io())
            .let { storage.updateBanners(it) }
            .observeOn(Schedulers.io())
            .map { Result(false, it) }
            .onErrorResumeNext { networkError ->
                storage.getBanners()
                        .doOnSuccess { if (it.isEmpty()) throw networkError }
                        .map { Result(true, it) }
            }

    class Result<T>(
            var isFromCache: Boolean,
            var data: T
    )

    private fun filterOffers(offers: List<Offer>, query: String): List<Offer> =
            offers.filter { offer ->
                val words = query.split(Regex(" +"))
                words.filter { it.isNotBlank() }
                        .forEach {
                    if ((offer.title ?: "").contains(it, true) ||
                            (offer.desc ?: "").contains(it, true)) {
                        return@filter true
                    }
                }
                false
            }
}
