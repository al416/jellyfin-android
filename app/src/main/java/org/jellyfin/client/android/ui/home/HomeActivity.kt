package org.jellyfin.client.android.ui.home

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.ActivityHomeBinding

private lateinit var binding: ActivityHomeBinding

class HomeActivity : DaggerAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
            R.string.home_activity_navigation_open_drawer, R.string.home_activity_navigation_close_drawer)
        binding.drawerLayout.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }
}