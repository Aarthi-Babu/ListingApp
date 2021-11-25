package com.example.listingapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.listingapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListingActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing)
    }


}