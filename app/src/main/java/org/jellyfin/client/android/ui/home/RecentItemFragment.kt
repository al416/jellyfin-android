package org.jellyfin.client.android.ui.home

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.load
import dagger.android.support.DaggerFragment
import org.jellyfin.client.android.databinding.FragmentRecentItemsBinding
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_HEIGHT
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.BLUR_HASH_BACKDROP_WIDTH
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_MEDIA_UUID
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_POSITION
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.ui.player.PlayerActivity
import org.jellyfin.client.android.ui.shared.BlurHashDecoder
import javax.inject.Inject

class RecentItemFragment : DaggerFragment() {

    companion object {
        fun newInstance(position: Int): RecentItemFragment {
            val bundle = bundleOf(BUNDLE_TAG_POSITION to position)
            val fragment = RecentItemFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    private lateinit var binding: FragmentRecentItemsBinding

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = requireArguments().getInt(BUNDLE_TAG_POSITION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.getHomePage().observe(viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.recentItems?.let { items ->
                        if (items.size > position) {
                            val item = resource.data?.recentItems?.get(position)
                            item?.let {card ->
                                binding.tvTitle.text = card.title
                                val bitmap = BlurHashDecoder.decode(card.blurHash, BLUR_HASH_BACKDROP_WIDTH, BLUR_HASH_BACKDROP_HEIGHT)
                                val drawable = BitmapDrawable(requireContext().resources, bitmap)
                                binding.background.load(card.imageUrl) {
                                    placeholder(drawable)
                                    error(drawable)
                                }
                                binding.btnPlay.setOnClickListener {
                                    val intent = Intent(requireActivity(), PlayerActivity::class.java)
                                    intent.putExtra(BUNDLE_TAG_MEDIA_UUID, card.uuid.toString())
                                    startActivity(intent)
                                }
                                binding.executePendingBindings()
                            }
                        }
                    }
                }
                // TODO: Display error message
                Status.ERROR -> {

                }
                // TODO: Display loading indicator
                Status.LOADING -> {

                }
            }
        })
    }

}
