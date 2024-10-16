package io.bimmergestalt.idriveconnectaddons.aaidriveha

import android.widget.TextView
import androidx.databinding.BindingAdapter
import io.bimmergestalt.idriveconnectkit.android.CDSLiveData


object DataBindingHelpers {
    /**
     * Format a CDSLiveData to a pretty-printed JSON string
     */
    @JvmStatic
    @BindingAdapter("android:text")
    fun setText(view: TextView, liveData: CDSLiveData) {
        val current = liveData.value
        if (current != null) {
            view.text = current.toString()
        } else {
            view.text = ""
        }
    }
}

