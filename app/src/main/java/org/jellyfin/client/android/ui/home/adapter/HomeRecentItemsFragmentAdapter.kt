package org.jellyfin.client.android.ui.home.adapter


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.jellyfin.client.android.ui.home.RecentItemFragment

class HomeRecentItemsFragmentAdapter(fragment: Fragment, adapterItemCount: Int) : FragmentStateAdapter(fragment) {

    var totalItems = adapterItemCount

    override fun getItemCount(): Int {
        return totalItems
    }

    override fun createFragment(position: Int): Fragment {
        return RecentItemFragment.newInstance(position)
    }

}
