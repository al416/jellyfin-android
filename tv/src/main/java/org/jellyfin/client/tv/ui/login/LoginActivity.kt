package org.jellyfin.client.tv.ui.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import org.jellyfin.client.tv.R
import org.jellyfin.client.tv.databinding.ActivityLoginBinding

class LoginActivity : FragmentActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
}