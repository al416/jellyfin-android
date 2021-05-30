package org.jellyfin.client.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.android.ui.home.HomeViewModel
import org.jellyfin.client.android.ui.home.RecentItemViewModel
import org.jellyfin.client.android.ui.home.library.LibraryViewModel
import org.jellyfin.client.android.ui.home.library_home.LibraryHomeViewModel
import org.jellyfin.client.android.ui.home.movie_details.MovieDetailsViewModel
import org.jellyfin.client.android.ui.home.season_details.SeasonDetailsViewModel
import org.jellyfin.client.android.ui.home.series_details.SeriesDetailsViewModel
import org.jellyfin.client.android.ui.login.LoginViewModel
import org.jellyfin.client.android.ui.login.add_server.AddServerViewModel
import org.jellyfin.client.android.ui.player.PlayerViewModel
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindsViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindsLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddServerViewModel::class)
    abstract fun bindsAddServerViewModel(addServerViewModel: AddServerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindsHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayerViewModel::class)
    abstract fun bindsPlayerViewModel(playerViewModel: PlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecentItemViewModel::class)
    abstract fun bindsRecentItemViewModel(recentItemViewModel: RecentItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MovieDetailsViewModel::class)
    abstract fun bindsMovieDetailsViewModel(movieDetailsViewModel: MovieDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SeriesDetailsViewModel::class)
    abstract fun bindsSeriesDetailsViewModel(seriesDetailsViewModel: SeriesDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LibraryViewModel::class)
    abstract fun bindsLibraryViewModel(libraryViewModel: LibraryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LibraryHomeViewModel::class)
    abstract fun bindsLibraryHomeViewModel(libraryHomeViewModel: LibraryHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SeasonDetailsViewModel::class)
    abstract fun bindsSeasonDetailsViewModel(seasonDetailsViewModel: SeasonDetailsViewModel): ViewModel
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)