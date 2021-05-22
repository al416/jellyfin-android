package org.jellyfin.client.android.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.jellyfin.client.android.ui.home.HomeFragment
import org.jellyfin.client.android.ui.home.RecentItemFragment
import org.jellyfin.client.android.ui.home.library.LibraryFragment
import org.jellyfin.client.android.ui.home.movie_details.MovieDetailsFragment
import org.jellyfin.client.android.ui.home.series_details.SeriesDetailsFragment
import org.jellyfin.client.android.ui.login.LoginFragment
import org.jellyfin.client.android.ui.login.add_server.AddServerDialog
import org.jellyfin.client.android.ui.login.add_server.AddServerFragment

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributesLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributesHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributesRecentItemFragment(): RecentItemFragment

    @ContributesAndroidInjector
    abstract fun contributesAddServerFragment(): AddServerFragment

    @ContributesAndroidInjector
    abstract fun contributesAddServerDialog(): AddServerDialog

    @ContributesAndroidInjector
    abstract fun contributesMovieDetailsFragment(): MovieDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributesSeriesDetailsFragment(): SeriesDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributesLibraryFragment(): LibraryFragment
}