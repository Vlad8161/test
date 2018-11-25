package com.example.test.model.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.example.test.model.room.entity.OfferRoom
import io.reactivex.Single

/**
 * Created by vlad on 25.11.18.
 */

@Dao
interface OfferDao {
    @Query("SELECT * FROM offer")
    fun getAllOffers(): Single<List<OfferRoom>>

    @Query("DELETE FROM offer")
    fun clearOffers()

    @Insert(onConflict = REPLACE)
    fun updateOffers(offers: List<OfferRoom>)
}