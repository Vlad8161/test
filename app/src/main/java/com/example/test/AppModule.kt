package com.example.test

import android.content.Context
import com.example.test.model.BASE_URL
import com.example.test.model.Model
import com.example.test.model.ServerApi
import com.example.test.model.Storage
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by vlad on 25.11.18.
 */

@Module
class AppModule(private val context: Context) {
    @Provides
    @Singleton
    fun provideModel(storage: Storage, serverApi: ServerApi): Model = Model(storage, serverApi)

    @Provides
    @Singleton
    fun provideStorage(): Storage = Storage(context)

    @Provides
    @Singleton
    fun provideServerApi(): ServerApi = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ServerApi::class.java)}