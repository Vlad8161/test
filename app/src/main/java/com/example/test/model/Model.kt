package com.example.test.model

import com.example.test.model.data.Banner
import com.example.test.model.data.Offer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Created by vlad on 24.11.18.
 */

class Model(
        private val storage: Storage,
        private val serverApi: ServerApi
) {

    fun getOffers(): Single<Result<List<Offer>>> = serverApi.getOffers()
            //.delay(7, TimeUnit.SECONDS)
            //.doOnSuccess { throw RuntimeException() }
            .subscribeOn(Schedulers.io())
            .let { storage.updateOffers(it) }
            .observeOn(Schedulers.io())
            .map { Result(false, it) }
            .onErrorResumeNext { networkError ->
                storage.getOffers()
                        .doOnSuccess { if (it.isEmpty()) throw networkError }
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
}
