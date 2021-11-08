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
    private var itemList: List<User>,
    private val clickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerViewAdapter.SampleViewHolders>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolders {
        val layoutView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item, null
        )
        return SampleViewHolders(layoutView)
    }

    override fun onBindViewHolder(holder: SampleViewHolders, position: Int) {
        Glide.with(context)
            .load(itemList[position].thumbnail)
            .centerCrop()
            .into(holder.image)
        holder.name.text =
            (itemList[position].firstName + " " + itemList[position].lastName)
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun filterList(filterList: List<User>) {
        itemList = filterList
        notifyDataSetChanged()
    }

    inner class SampleViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var name: TextView = itemView.findViewById(R.id.name)
        var cardView: CardView = itemView.findViewById(R.id.card_view)
        fun bind(item: User) {
            cardView.setOnClickListener {
                clickListener(adapterPosition)

            }

        }
    }


}