package com.example.listingapp.utils

import android.app.Dialog
import android.content.Context
import android.view.View
import com.example.listingapp.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


class ProgressDialog @AssistedInject constructor(@Assisted context: Context) : View(context) {
    private var progress: Dialog = Dialog(context)

    init {
        progress.setContentView(R.layout.dialog_progress)
        progress.setCancelable(true)
        progress.setCanceledOnTouchOutside(false)
        progress.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun showLoading() {
        show(true)
    }

    fun hideLoading() {
        show(false)
    }

    private fun show(isLoading: Boolean) {
        if (isLoading) {
            if (!progress.isShowing)
                progress.show()
        } else {
            progress.dismiss()
        }
    }

}