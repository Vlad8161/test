package com.example.test.model.room.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.example.test.model.data.Banner

/**
 * Created by vlad on 25.11.18.
 */

@Entity(tableName = "banner")
class BannerRoom(
        @PrimaryKey @ColumnInfo(name = "id") var id: String,
        @ColumnInfo(name = "title") var title: String?,
        @ColumnInfo(name = "description") var description: String?,
        @ColumnInfo(name = "image") var image: String?
) {
    constructor(banner: Banner) : this(
            banner.id,
            banner.title,
            banner.desc,
            banner.image
    )
}