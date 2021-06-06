package org.jellyfin.client.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.HomeNavigationDirections
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.ActivityHomeBinding
import org.jellyfin.client.android.domain.constants.ItemType
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.login.LoginActivity
import org.jellyfin.client.android.ui.login.LoginViewModel
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
    }

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
    }

    private val navController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navHostFragment.navController
    }

    private val appBarConfiguration by lazy {
        AppBarConfiguration(navController.graph, binding.drawerLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
            R.string.home_activity_navigation_open_drawer, R.string.home_activity_navigation_close_drawer)
        binding.drawerLayout.addDrawerListener(toggle)
        binding.leftNav.setNavigationItemSelectedListener(this)

        binding.toolbar.setNavigationOnClickListener {
            NavigationUI.navigateUp(navController, appBarConfiguration)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
        navController.addOnDestinationChangedListener(this)

        loginViewModel.getLoginState().observe(this, {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        resource.data?.let {login ->
                            if (login.accessToken == null) {
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    else -> {
                        // do nothing here
                    }
                }
            }
        })

        homeViewModel.getLibraries().observe(this, {
            when (it.status) {
                Status.SUCCESS -> {
                    // Unlock the drawer
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    val mediaSubMenu = binding.leftNav.menu.getItem(1).subMenu
                    mediaSubMenu.clear()
                    it.data?.forEachIndexed {index, library ->
                        val item = mediaSubMenu.add(R.id.media_group, library.id, 0, library.title)
                        val icon = if (library.type == ItemType.MOVIE) R.drawable.ic_movies else R.drawable.ic_tv_shows
                        item.icon = ContextCompat.getDrawable(this, icon)
                    }
                }
                Status.LOADING -> {
                    // Lock the drawer then the list of libraries is loading
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                Status.ERROR -> {

                }
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectItem(item)
        return when (item.itemId) {
            R.id.logout -> {
                loginViewModel.doUserLogout()
                binding.drawerLayout.close()
                true
            }
            R.id.home -> {
                val action = HomeNavigationDirections.actionHome()
                navController.navigate(action)
                binding.drawerLayout.close()
                true
            }
            else -> {
                val libraries = homeViewModel.getLibraries().value?.data
                if (libraries != null) {
                    val library = libraries[item.itemId]
                    navController.navigate(
                        HomeNavigationDirections.actionLibrary(library.title ?: "", library),
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                    binding.drawerLayout.close()
                }
                true
            }
        }
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments[0]
        // If the current fragment is the Home fragment then move the task to the background, otherwise behave like a normal back press
        if (currentFragment is HomeFragment) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        if (destination.id == R.id.home_fragment) {
            binding.toolbar.title = getString(R.string.app_name)
            val homeMenuItem = binding.leftNav.menu.getItem(0)
            selectItem(homeMenuItem)
        }
    }

    private fun selectItem(item: MenuItem) {
        val homeMenuItem = binding.leftNav.menu.getItem(0)
        homeMenuItem.isChecked = false
        val mediaSubMenu = binding.leftNav.menu.getItem(1).subMenu
        for (i in 0 until mediaSubMenu.size()) {
            mediaSubMenu.getItem(i).isChecked = false
        }
        item.isChecked = true
    }
}
