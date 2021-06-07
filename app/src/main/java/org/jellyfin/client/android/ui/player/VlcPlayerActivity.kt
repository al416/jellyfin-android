package org.jellyfin.client.android.ui.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.ActivityVlcPlayerBinding
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import javax.inject.Inject

class VlcPlayerActivity : DaggerAppCompatActivity(), MediaPlayer.EventListener {

    companion object {
        const val USE_TEXTURE_VIEW = false
        const val ENABLE_SUBTITLES = true
    }

    private lateinit var binding: ActivityVlcPlayerBinding

    private lateinit var libVlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mediaId: String

    // TODO: Move this logic into the ViewModel so it survives rotation changes
    private var orientationLocked = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val playerViewModel: PlayerViewModel by lazy {
        val model = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)
        model.initialize(mediaId)
        model
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vlc_player)

        mediaId = intent.getStringExtra(Tags.BUNDLE_TAG_MEDIA_UUID) ?: throw Exception("MediaId required to play media")

        val args = mutableListOf<String>()
        args.add("-vvv")    // verbosity
        args.add("--aout=opensles")
        args.add("--audio-time-stretch")

        libVlc = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVlc)

        binding.overlay.setOnClickListener {
            hideOverlay()
        }

        binding.container.setOnClickListener {
            displayOverlay()
        }

        binding.btnScreenRotation.setOnClickListener {
            if (orientationLocked) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                binding.btnScreenRotation.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_screen_rotation, null))
            } else {
                val currentOrientation = resources.configuration.orientation
                val drawable = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    R.drawable.ic_screen_lock_landscape else R.drawable.ic_screen_lock_portrait
                binding.btnScreenRotation.setImageDrawable(ResourcesCompat.getDrawable(resources, drawable, null))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            }
            orientationLocked = !orientationLocked
        }

        binding.btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                binding.btnPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
                mediaPlayer.play()
            }
        }

        mediaPlayer.setEventListener(this)

        playerViewModel.getVideoPlaybackInformation().observe(this, { resource ->
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        libVlc.release()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        mediaPlayer.detachViews()
    }

    override fun onResume() {
        super.onResume()
        //hideSystemUi()
    }

    private fun hideSystemUi() {
        binding.videoLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun displaySystemUi() {
        binding.videoLayout.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    private fun playVideo(videoPlaybackInformation: VideoPlaybackInformation) {
        if (videoPlaybackInformation.url.isNullOrBlank()) {
            return
        }
        val playableUrl = videoPlaybackInformation.url!!
        mediaPlayer.attachViews(binding.videoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
        try {
            val media = Media(libVlc, Uri.parse(playableUrl))
            media.parse(IMedia.Parse.ParseNetwork)
            if (media.subItems() != null && media.subItems().count > 0 && media.subItems().getMediaAt(0) != null) {
                mediaPlayer.media = media.subItems().getMediaAt(0)
            } else {
                mediaPlayer.media = media
            }
            // Options for network playback only
            media.addOption(":network-caching=5000")
            media.addOption(":clock-jitter=0")
            media.addOption(":clock-synchro=0")
            // Enable hardware decoding
            media.setHWDecoderEnabled(true, false)
            media.release()
            mediaPlayer.play()
        } catch (e: Exception) {
            // TODO: Handle error
        }
    }

    override fun onEvent(event: MediaPlayer.Event?) {
        event?.type.let {
            when (it) {
                MediaPlayer.Event.Playing -> {
                    hideOverlay()
                    binding.btnPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
                }
                MediaPlayer.Event.Paused -> {
                    displayOverlay()
                    binding.btnPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))
                }
            }
        }
    }

    private fun hideOverlay() {
        hideSystemUi()
        binding.overlay.visibility = View.INVISIBLE
    }

    private fun displayOverlay() {
        displaySystemUi()
        binding.overlay.visibility = View.VISIBLE
    }
}