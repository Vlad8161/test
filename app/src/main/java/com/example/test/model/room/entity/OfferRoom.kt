package com.example.test.model.room.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.example.test.model.data.Offer

/**
 * Created by vlad on 25.11.18.
 */

@Entity(tableName = "offer")
class OfferRoom(
        @PrimaryKey @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "title") var title: String?,
        @ColumnInfo(name = "description") var description: String?,
        @ColumnInfo(name = "image") var image: String?,
        @ColumnInfo(name = "type") var type: String,
        @ColumnInfo(name = "group") var group: String,
        @ColumnInfo(name = "price") var price: Float?,
        @ColumnInfo(name = "discount") var discount: Float?
) {
    constructor(offer: Offer) : this(
            offer.id,
            offer.title,
            offer.desc,
            offer.image,
            offer.type,
            offer.groupName,
            offer.price,
            offer.discount
    )
}