package org.jellyfin.client.android.ui.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.ActivityLoginBinding

class LoginActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
}