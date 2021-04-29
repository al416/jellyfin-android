package org.jellyfin.client.android.ui.player

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.constants.Tags.BUNDLE_TAG_MEDIA_UUID
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.VideoPlayType
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import javax.inject.Inject

class PlayerActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private lateinit var playerView: PlayerView
    private lateinit var mediaId: String
    private lateinit var userId: String

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val playerViewModel: PlayerViewModel by lazy {
        val model = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)
        model.initialize(mediaId, userId)
        model
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mediaId = intent.getStringExtra(BUNDLE_TAG_MEDIA_UUID) ?: ""
        userId = "" // TODO: retrieve the userId from a user repository
        playerView = findViewById(R.id.player_view)
        playerView.player = exoPlayer

        playerViewModel.getVideoPlaybackInformation().observe(this, Observer {resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let {
                        if (it.url.isNullOrBlank()) {
                            // Display error and leave
                            Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            playVideo(it)
                        }
                    }
                }
                Status.LOADING -> {

                }
                Status.ERROR -> {

                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.stop()
        exoPlayer.release()
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun playVideo(videoPlaybackInformation: VideoPlaybackInformation) {
        if (videoPlaybackInformation.url.isNullOrBlank()) {
            return
        }
        val playableUrl = videoPlaybackInformation.url!!
        // TODO: Set correct user agent
        val userAgent = Util.getUserAgent(
            this,
            getString(R.string.app_name)
        )

        val mediaItem = MediaItem.fromUri(playableUrl)
        val mediaSource = when (videoPlaybackInformation.videoPlayType) {
            VideoPlayType.DIRECT_PLAY -> {
                ProgressiveMediaSource
                    .Factory(
                        DefaultDataSourceFactory(this, userAgent),
                        DefaultExtractorsFactory()
                    ).createMediaSource(mediaItem)
            }
            else -> {
                HlsMediaSource.Factory(DefaultDataSourceFactory(this, userAgent)).createMediaSource(mediaItem)
            }
        }

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }
}