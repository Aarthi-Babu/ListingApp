package com.example.listingapp.view

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
    private val clickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerViewAdapter.SampleViewHolders>() {
    private var itemList: ArrayList<User>? = null

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
        itemList = listItems
        notifyDataSetChanged()
    }

    inner class SampleViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var name: TextView = itemView.findViewById(R.id.name)
        var cardView: CardView = itemView.findViewById(R.id.card_view)
        fun bind(item: User) {
            Glide.with(itemView.context)
                .load(item.thumbnail)
                .placeholder(R.drawable.progress_animation)
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