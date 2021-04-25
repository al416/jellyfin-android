package org.jellyfin.client.android.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.ViewCompat
import com.google.android.material.textfield.TextInputEditText
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.R

class LoginFragment : DaggerFragment(), View.OnClickListener {

    private lateinit var textBaseUrl: TextInputEditText
    private lateinit var textUsername: TextInputEditText
    private lateinit var textPassword: TextInputEditText
    private lateinit var buttonLogin: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textBaseUrl = ViewCompat.requireViewById(view, R.id.textBaseUrl)
        textUsername = ViewCompat.requireViewById(view, R.id.textUsername)
        textPassword = ViewCompat.requireViewById(view, R.id.textPassword)

        buttonLogin = ViewCompat.requireViewById(view, R.id.buttonLogin)
        buttonLogin.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonLogin -> {

            }
        }
    }

}