package com.example.test.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.test.R

/**
 * Created by vlad on 24.11.18.
 */

class FiltersAdapter : RecyclerView.Adapter<FiltersAdapter.FilterViewHolder>() {
    var data: List<FilterItem> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val item = data[position]
        holder.tvName.text = item.text
        if (item.checked) {
            holder.tvName.setBackgroundResource(R.drawable.bg_filter_selected)
            holder.tvName.setTextColor(ContextCompat.getColor(holder.tvName.context, R.color.btnFilterTextColorSelected))
        } else {
            holder.tvName.setBackgroundResource(R.drawable.bg_filter_not_selected)
            holder.tvName.setTextColor(ContextCompat.getColor(holder.tvName.context, R.color.btnFilterTextColorNotSelected))
        }
        holder.tvName.setOnClickListener {
            data.forEachIndexed { i, item ->
                if (item.checked) {
                    item.checked = false
                    notifyItemChanged(i)
                }
            }
            item.checked = true
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder =
            FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_filter, parent, false))

    class FilterViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val tvName: TextView = v as TextView
    }

    class FilterItem(
            val text: String,
            var checked: Boolean
    )
}