package com.example.test.model.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.example.test.model.room.dao.BannerDao
import com.example.test.model.room.dao.OfferDao
import com.example.test.model.room.entity.BannerRoom
import com.example.test.model.room.entity.OfferRoom

/**
 * Created by vlad on 25.11.18.
 */

@Database(entities = arrayOf(OfferRoom::class, BannerRoom::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getOfferDao(): OfferDao
    abstract fun getBannerDao(): BannerDao
}