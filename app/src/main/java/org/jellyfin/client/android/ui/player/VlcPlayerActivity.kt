package org.jellyfin.client.android.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.VLCVideoLayout
import javax.inject.Inject

class VlcPlayerActivity : DaggerAppCompatActivity() {

    companion object {
        const val USE_TEXTURE_VIEW = false
        const val ENABLE_SUBTITLES = true
    }

    private lateinit var videoLayout: VLCVideoLayout
    private lateinit var libVlc: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mediaId: String

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val playerViewModel: PlayerViewModel by lazy {
        val model = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)
        model.initialize(mediaId)
        model
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vlc_player)
        mediaId = intent.getStringExtra(Tags.BUNDLE_TAG_MEDIA_UUID) ?: throw Exception("MediaId required to play media")

        val args = mutableListOf<String>()
        args.add("-vvv")    // verbosity
        args.add("--aout=opensles")
        args.add("--audio-time-stretch")

        libVlc = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVlc)

        videoLayout = findViewById(R.id.video_layout)

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
        hideSystemUi()
    }

    private fun hideSystemUi() {
        videoLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
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
        mediaPlayer.attachViews(videoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
        try {
            val media = Media(libVlc, Uri.parse(playableUrl))
            media.parse(IMedia.Parse.ParseNetwork)
            if (media.subItems() != null && media.subItems().count > 0 && media.subItems().getMediaAt(0) != null) {
                mediaPlayer.media = media.subItems().getMediaAt(0)
            } else {
                mediaPlayer.media = media
            }
            media.release()
            mediaPlayer.play()
        } catch (e: Exception) {
            // TODO: Handle error
        }
    }
}
