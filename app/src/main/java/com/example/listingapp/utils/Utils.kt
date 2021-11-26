package com.example.listingapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return networkCapabilities != null &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}



