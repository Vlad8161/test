package com.example.test.model.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.example.test.model.room.entity.BannerRoom
import com.example.test.model.room.entity.OfferRoom
import io.reactivex.Single

/**
 * Created by vlad on 25.11.18.
 */

@Dao
interface BannerDao {
    @Query("SELECT * FROM banner")
    fun getAllBanners(): Single<List<BannerRoom>>

    @Query("DELETE FROM banner")
    fun clearBanners()

    @Insert(onConflict = REPLACE)
    fun updateBanners(offers: List<BannerRoom>)
}