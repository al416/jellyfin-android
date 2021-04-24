package org.jellyfin.client.android.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.jellyfin.client.android.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}