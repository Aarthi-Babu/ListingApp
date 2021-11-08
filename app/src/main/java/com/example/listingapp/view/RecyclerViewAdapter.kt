package com.example.listingapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listingapp.R
import com.example.listingapp.database.User


class RecyclerViewAdapter(
    private val context: Context,
    private var itemList: List<User>
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
        holder.authorName.text =
            (itemList[position].firstName + " " + itemList[position].lastName)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun filterList(filterList: List<User>) {
        itemList = filterList
        notifyDataSetChanged()
    }

    inner class SampleViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var image: ImageView
        var authorName: TextView
        override fun onClick(view: View) {
            Toast.makeText(
                view.context,
                "Clicked Position = $adapterPosition", Toast.LENGTH_SHORT
            )
                .show()
        }

        init {
            itemView.setOnClickListener(this)
            authorName = itemView.findViewById(R.id.AuthorName)
            image = itemView.findViewById(R.id.image)
        }
    }

}