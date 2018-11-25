package com.example.test.ui.adapter

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.test.R
import com.example.test.model.data.Offer
import com.example.test.model.data.OfferTypes
import com.example.test.utils.SwipeButtonTouchListener
import com.squareup.picasso.Picasso

/**
 * Created by vlad on 24.11.18.
 */

class OffersAdapter(
        private val onOfferClicked: (offerId: String) -> Unit,
        private val onOfferRemoveClicked: (offerId: String) -> Unit
) : RecyclerView.Adapter<OffersAdapter.BaseViewHolder>() {
    enum class ViewType {
        GROUP_HEADER,
        OFFER,
    }

    var data: List<Item> = ArrayList()
        set(value) {
            val diff = DiffUtil.calculateDiff(OfferDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            ViewType.GROUP_HEADER.ordinal -> GroupHeaderViewHolder.create(parent)
            ViewType.OFFER.ordinal -> OfferViewHolder.create(parent, onOfferClicked, onOfferRemoveClicked)
            else -> throw RuntimeException("Unsupported type $viewType")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is GroupHeaderItem -> ViewType.GROUP_HEADER.ordinal
        is OfferItem -> ViewType.OFFER.ordinal
        else -> throw RuntimeException("Unsupported view type at position $position")
    }

    open class Item

    class GroupHeaderItem(
            val name: String
    ) : Item()

    class OfferItem(
            val id: String,
            val type: String,
            val imageUrl: String?,
            val title: String?,
            val description: String?,
            val discount: Float?,
            val price: Float
    ) : Item() {
        constructor(item: Offer) : this(
                item.id,
                item.type,
                item.image,
                item.title,
                item.desc,
                item.discount,
                item.price ?: 0f
        )
    }

    abstract class BaseViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(item: Item)
    }

    private class GroupHeaderViewHolder(v: View) : BaseViewHolder(v) {
        val tvTitle = v as TextView

        override fun bind(item: Item) {
            item as GroupHeaderItem
            tvTitle.text = item.name
        }

        companion object {
            fun create(parent: ViewGroup): GroupHeaderViewHolder =
                    GroupHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_group_header, parent, false))
        }
    }

    private class OfferViewHolder(
            v: View,
            val onOfferClicked: (offerId: String) -> Unit,
            val onOfferRemoveClicked: (offerId: String) -> Unit
    ) : BaseViewHolder(v) {
        val ivImage: ImageView = v.findViewById(R.id.ivImage)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvDescription: TextView = v.findViewById(R.id.tvDescription)
        val tvDiscount: TextView = v.findViewById(R.id.tvDiscount)
        val tvBasePrice: TextView = v.findViewById(R.id.tvBasePrice)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val strike: View = v.findViewById(R.id.strike)
        val content: View = v.findViewById(R.id.content)

        override fun bind(item: Item) {
            item as OfferItem
            Picasso.get().cancelRequest(ivImage)

            if (item.imageUrl != null) {
                Picasso.get().load(item.imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.placeholder)
            }

            tvTitle.text = item.title ?: itemView.context.resources.getString(R.string.no_title)
            tvDescription.text = item.description ?: itemView.context.resources.getString(R.string.no_description)

            if (item.type == OfferTypes.PRODUCT) {
                if (item.discount != null && item.discount > 0.0001f) {
                    tvDiscount.text = "-${Math.round(item.discount * 100)}%"
                    tvDiscount.visibility = View.VISIBLE

                    tvBasePrice.text = "${Math.round(item.price)} ₽"
                    tvBasePrice.visibility = View.VISIBLE
                    strike.visibility = View.VISIBLE

                    tvPrice.text = "${Math.round(item.price * (1 - item.discount))} ₽"
                    tvPrice.visibility = View.VISIBLE
                } else {
                    tvDiscount.visibility = View.INVISIBLE
                    tvBasePrice.visibility = View.INVISIBLE
                    strike.visibility = View.INVISIBLE

                    tvPrice.text = "${Math.round(item.price)} ₽"
                    tvPrice.visibility = View.VISIBLE
                }
            } else {
                tvPrice.visibility = View.INVISIBLE
                tvBasePrice.visibility = View.INVISIBLE
                tvDiscount.visibility = View.INVISIBLE
                strike.visibility = View.INVISIBLE
            }

            content.setOnTouchListener(SwipeButtonTouchListener(itemView.context))
            content.setOnClickListener { onOfferClicked(item.id) }
            itemView.setOnClickListener { onOfferRemoveClicked(item.id) }
        }

        companion object {
            fun create(
                    parent: ViewGroup,
                    onOfferClicked: (offerId: String) -> Unit,
                    onOfferRemoveClicked: (offerId: String) -> Unit
            ): OfferViewHolder =
                    OfferViewHolder(
                            LayoutInflater.from(parent.context).inflate(R.layout.adapter_offer,
                                    parent, false),
                            onOfferClicked,
                            onOfferRemoveClicked
                    )
        }
    }

    private class OfferDiffUtilCallback(
            val oldList: List<Item>,
            val newList: List<Item>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return if (oldItem::class == newItem::class) {
                when (oldItem) {
                    is GroupHeaderItem -> oldItem.name == (newItem as GroupHeaderItem).name
                    is OfferItem -> oldItem.id == (newItem as OfferItem).id
                    else -> false
                }
            } else {
                false
            }
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

            return true
        }
    }
}