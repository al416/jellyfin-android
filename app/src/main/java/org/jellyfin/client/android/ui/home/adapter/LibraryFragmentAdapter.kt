package org.jellyfin.client.android.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jellyfin.client.android.domain.models.Library
import org.jellyfin.client.android.domain.models.display_model.Genre
import org.jellyfin.client.android.ui.home.library.LibraryFragment

@ExperimentalCoroutinesApi
class LibraryFragmentAdapter(fragment: Fragment, val library: Library, adapterItemCount: Int) : FragmentStateAdapter(fragment) {

    var totalItems = adapterItemCount
    var genres = mutableListOf<Genre>()

    override fun getItemCount(): Int {
        return totalItems
    }

    override fun createFragment(position: Int): Fragment {
        val genre = genres[position]
        return LibraryFragment.newInstance(library = library, genre = genre)
    }

}
