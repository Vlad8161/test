package com.example.test.model

import android.arch.persistence.room.Room
import android.content.Context
import com.example.test.model.data.Banner
import com.example.test.model.data.Offer
import com.example.test.model.room.AppDatabase
import com.example.test.model.room.entity.BannerRoom
import com.example.test.model.room.entity.OfferRoom
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

/**
 * Created by vlad on 25.11.18.
 */

class Storage(context: Context) {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "database.db").build()
    private val dbScheduler = Schedulers.from(Executors.newSingleThreadExecutor())
    private val offerDao = db.getOfferDao()
    private val bannerDao = db.getBannerDao()

    fun getOffers(): Single<List<Offer>> = offerDao.getAllOffers()
            .map { it.map { Offer(it) } }
            .subscribeOn(dbScheduler)

    fun updateOffers(offers: Single<List<Offer>>): Single<List<Offer>> = offers
            .observeOn(dbScheduler)
            .doOnSuccess {
                offerDao.clearOffers()
                offerDao.updateOffers(it.map { OfferRoom(it) })
            }

    fun getBanners(): Single<List<Banner>> = bannerDao.getAllBanners()
            .map { it.map { Banner(it) } }
            .subscribeOn(dbScheduler)

    fun updateBanners(banners: Single<List<Banner>>): Single<List<Banner>> = banners
            .observeOn(dbScheduler)
            .doOnSuccess {
                bannerDao.clearBanners()
                bannerDao.updateBanners(it.map { BannerRoom(it) })
            }
}