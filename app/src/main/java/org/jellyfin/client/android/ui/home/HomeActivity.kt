package org.jellyfin.client.android.ui.home

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R

class HomeActivity : DaggerAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }
}