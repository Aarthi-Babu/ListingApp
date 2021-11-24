package com.example.listingapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listingapp.R
import com.example.listingapp.database.User


class RecyclerViewAdapter(
    private val context: Context,
    private val clickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerViewAdapter.SampleViewHolders>() {
    private var itemList: ArrayList<User>? = null
    fun populateData(list: ArrayList<User>) {
        itemList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolders {
        val layoutView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item, null
        )
        return SampleViewHolders(layoutView)
    }

    override fun onBindViewHolder(holder: SampleViewHolders, position: Int) {
        itemList?.get(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }

    fun filterList(filterList: ArrayList<User>) {
        itemList = filterList
        notifyDataSetChanged()
    }

    fun addData(listItems: ArrayList<User>) {
        val size = itemList?.size ?: 0
        itemList = listItems
        val sizeNew = listItems.size
        notifyDataSetChanged()
//        notifyItemRangeChanged(size, sizeNew)
    }

    inner class SampleViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var name: TextView = itemView.findViewById(R.id.name)
        var cardView: CardView = itemView.findViewById(R.id.card_view)
        fun bind(item: User) {
            Glide.with(context)
                .load(item.thumbnail)
                .centerCrop()
                .into(image)
            name.text =
                (item.firstName + " " + item.lastName)
            cardView.setOnClickListener {
                clickListener(adapterPosition)
            }
        }
    }

}