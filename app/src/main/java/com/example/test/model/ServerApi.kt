package com.example.test.model

import com.example.test.model.data.Banner
import com.example.test.model.data.Offer
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Created by vlad on 24.11.18.
 */

val BASE_URL = "http://s3.eu-central-1.amazonaws.com"

interface ServerApi {

    @GET("/sl.files/offers.json")
    fun getOffers(): Single<List<Offer>>

    @GET("/sl.files/banners.json")
    fun getBanners(): Single<List<Banner>>
}