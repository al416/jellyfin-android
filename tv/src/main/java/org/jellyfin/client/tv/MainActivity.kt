package org.jellyfin.client.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import org.jellyfin.client.android.domain.models.display_model.Server
import org.jellyfin.client.android.domain.repository.LoginRepository
import org.jellyfin.client.tv.base.App
import org.jellyfin.client.tv.di.AppComponent
import org.jellyfin.client.tv.di.AppModule
import org.jellyfin.client.tv.di.DaggerAppComponent
import org.jellyfin.client.tv.ui.login.LoginViewModel
import javax.inject.Inject

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var loginRepo: LoginRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        val app = application as App
        val appModule = AppModule(app)
        val component = DaggerAppComponent.builder().appModule(appModule).build()
        component.inject(app)

        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }


        val server = Server(id = 1, name = "Test", "", displayOrder = 1)
        loginViewModel.doUserLogin(
            server = server,
            username = "demo",
            password = ""
        )

    }
}