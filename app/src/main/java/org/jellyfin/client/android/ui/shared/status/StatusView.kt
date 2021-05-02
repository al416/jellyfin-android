package org.jellyfin.client.android.ui.shared.status

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.jellyfin.client.android.R

class StatusView(context: Context, attrs: AttributeSet) : SwipeRefreshLayout(context, attrs) {

    private val statusText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.status_view, this, true)
        statusText = findViewById(R.id.txtStatus)
    }

    fun setStatusText(text: String) {
        statusText.text = text
    }

}