package com.example.listingapp.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.WindowManager
import androidx.core.content.ContextCompat

fun isNetworkAvailable(mContext: Context?): Boolean {
    val connectivityManager =
        mContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting == true
}

fun Activity.setStatusBarColor(color: Int) {
    window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window?.statusBarColor = ContextCompat.getColor(this, color)
}



