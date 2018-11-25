package com.example.test.ui.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.test.R
import com.example.test.model.data.Banner
import com.squareup.picasso.Picasso

/**
 * Created by vlad on 24.11.18.
 */

class BannersAdapter : PagerAdapter() {
    var data: List<BannerItem> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = data.size

    override fun getItemPosition(`object`: Any?): Int = POSITION_NONE

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.adapter_banner, container, false)
        val item = data[position]

        val ivImage = view.findViewById<ImageView>(R.id.ivImage)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)

        if (item.imageUrl != null) {
            Picasso.get().load(item.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(ivImage)
        } else {
            ivImage.setImageResource(R.drawable.placeholder)
        }

        tvTitle.text = item.title ?: view.context.getString(R.string.no_title)
        tvDescription.text = item.description ?: view.context.getString(R.string.no_description)

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    class BannerItem(
            val id: String,
            val title: String?,
            val description: String?,
            val imageUrl: String?
    ) {
        constructor(banner: Banner) : this(
                banner.id,
                banner.title,
                banner.desc,
                banner.image
        )
    }
}