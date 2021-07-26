package org.jellyfin.client.android.ui.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import org.jellyfin.client.android.R
import org.jellyfin.client.android.databinding.ActivityVlcPlayerBinding
import org.jellyfin.client.android.domain.constants.ConfigurationConstants.ENABLE_SUBTITLES
import org.jellyfin.client.android.domain.constants.Tags
import org.jellyfin.client.android.domain.extensions.formatTime
import org.jellyfin.client.android.domain.extensions.getSubtitleDescription
import org.jellyfin.client.android.domain.models.Status
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IMedia
import javax.inject.Inject

class VlcPlayerActivity : DaggerAppCompatActivity(),
    MediaPlayer.EventListener,
    SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    companion object {
        const val USE_TEXTURE_VIEW = false
    }

    private lateinit var binding: ActivityVlcPlayerBinding

    private var libVlc: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vlc_player)
        setContentView(binding.root)
        mediaId = intent.getStringExtra(Tags.BUNDLE_TAG_MEDIA_UUID) ?: throw Exception("MediaId required to play media")

        setControlsInitialStates()
        setupStaticControls()

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
        mediaPlayer?.release()
        libVlc?.release()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.detachViews()
    }

    override fun onResume() {
        super.onResume()
        //hideSystemUi()
    }

    private fun setupStaticControls() {
        binding.overlay.setOnClickListener {
            hideOverlay()
        }

        binding.container.setOnClickListener {
            displayOverlay()
        }

        binding.btnScreenRotation.setOnClickListener {
            if (playerViewModel.orientationLocked) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                binding.btnScreenRotation.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_screen_rotation, null))
            } else {
                val currentOrientation = resources.configuration.orientation
                val drawable = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    R.drawable.ic_screen_lock_landscape else R.drawable.ic_screen_lock_portrait
                binding.btnScreenRotation.setImageDrawable(ResourcesCompat.getDrawable(resources, drawable, null))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            }
            playerViewModel.orientationLocked = !playerViewModel.orientationLocked
        }

        binding.btnPlayPause.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            } else {
                binding.btnPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
                mediaPlayer?.play()
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(this)
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
        playerViewModel.url = videoPlaybackInformation.url!!
        try {
            playVideo()
        } catch (e: Exception) {
            // TODO: Handle error
        }
    }

    override fun onEvent(event: MediaPlayer.Event?) {
        event?.type.let {
            when (it) {
                MediaPlayer.Event.Playing -> {
                    if (!playerViewModel.mediaParsingComplete) {
                        getVideoInformation()
                        binding.btnPlayPause.isClickable = true
                        binding.btnPlayPause.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
                    hideOverlay()
                    binding.seekbar.progress = playerViewModel.currentPosition.toInt()
                    mediaPlayer?.time = playerViewModel.currentPosition
                    binding.btnPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
                }
                MediaPlayer.Event.Paused -> {
                    displayOverlay()
                    binding.btnPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))
                }
                MediaPlayer.Event.TimeChanged -> {
                    mediaPlayer?.let {player ->
                        playerViewModel.currentPosition = player.time
                        binding.seekbar.progress = player.time.toInt()
                        binding.tvDuration.text = getString(R.string.media_duration, player.time.formatTime(), player.media?.duration?.formatTime())
                    }
                }
                MediaPlayer.Event.Opening -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                else -> {

                }
            }
        }
    }

    private fun setControlsInitialStates() {
        binding.btnSubtitles.isEnabled = false
        binding.btnSubtitles.isClickable = false
        binding.btnAudioTrack.isEnabled = false
        binding.btnAudioTrack.isClickable = false
        binding.seekbar.isEnabled = false
        binding.seekbar.isClickable = false
        binding.btnPlayPause.isClickable = false
        binding.btnPlayPause.isEnabled = false
    }

    private fun hideOverlay() {
        hideSystemUi()
        binding.overlay.visibility = View.INVISIBLE
    }

    private fun displayOverlay() {
        displaySystemUi()
        binding.overlay.visibility = View.VISIBLE
    }

    override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.time = progress.toLong()
            } else {
                mediaPlayer?.play()
                mediaPlayer?.time = progress.toLong()
                mediaPlayer?.pause()
            }
        }
    }

    override fun onStartTrackingTouch(seekbar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekbar: SeekBar?) {

    }

    private fun getVideoInformation() {
        playerViewModel.mediaParsingComplete = true
        mediaPlayer?.let { player ->
            player.media?.let { media ->
                for (i in 0 until media.trackCount) {
                    val track = media.getTrack(i)
                    if (track is IMedia.SubtitleTrack) {
                        playerViewModel.subtitleTracks.add(track)
                    } else if (track is IMedia.AudioTrack) {
                        playerViewModel.audioTracks.add(track)
                    }
                }
            }
        }
        setupDynamicControls()
    }

    private fun setupDynamicControls() {
        // Set seekbar duration
        mediaPlayer?.let { player ->
            player.media?.let { media ->
                binding.seekbar.isEnabled = true
                binding.seekbar.isClickable = true
                binding.seekbar.max = media.duration.toInt()
                binding.tvDuration.text = getString(R.string.media_duration, getString(R.string.initial_time), media.duration.formatTime())
            }
        }
        if (!playerViewModel.subtitleTracks.isNullOrEmpty()) {
            binding.btnSubtitles.isEnabled = true
            binding.btnSubtitles.isClickable = true
            binding.btnSubtitles.setOnClickListener(this)
            if (playerViewModel.selectedSubtitleTrack == null) {
                mediaPlayer?.let { player ->
                    val item = player.media?.getTrack(player.spuTrack)
                    playerViewModel.selectedSubtitleTrack = playerViewModel.subtitleTracks.indexOf(item)
                }
            }
        }
        if (!playerViewModel.audioTracks.isNullOrEmpty()) {
            binding.btnAudioTrack.isEnabled = true
            binding.btnAudioTrack.isClickable = true
            binding.btnAudioTrack.setOnClickListener(this)
            if (playerViewModel.selectedAudioTrack == null) {
                mediaPlayer?.let { player ->
                    val item = player.media?.getTrack(player.audioTrack)
                    playerViewModel.selectedAudioTrack = playerViewModel.audioTracks.indexOf(item)
                }
            }
        }
    }

    private fun subtitleSelected(subtitleTrackIndex: Int?) {
        playerViewModel.selectedSubtitleTrack = subtitleTrackIndex
        mediaPlayer?.stop()
        mediaPlayer?.detachViews()
        mediaPlayer?.release()
        libVlc?.release()
        playVideo()
    }

    private fun audioSelected(audioTrackIndex: Int?) {
        playerViewModel.selectedAudioTrack = audioTrackIndex
        mediaPlayer?.stop()
        mediaPlayer?.detachViews()
        mediaPlayer?.release()
        libVlc?.release()
        playVideo()
    }

    private fun playVideo() {
        val args = mutableListOf<String>()
        args.add("-vvv")    // verbosity
        args.add("--aout=opensles")
        args.add("--audio-time-stretch")

        playerViewModel.selectedSubtitleTrack?.let {
            args.add("--sub-track=$it")   // first track is 0, second track is 1, etc
        }

        playerViewModel.selectedAudioTrack?.let {
            args.add("--audio-track=$it")   // first track is 0, second track is 1, etc
        }

        libVlc = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVlc)
        val media = Media(libVlc, Uri.parse(playerViewModel.url))
        media.parse(IMedia.Parse.ParseNetwork)
        mediaPlayer?.setEventListener(this)
        mediaPlayer?.attachViews(binding.videoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)

        if (media.subItems() != null && media.subItems().count > 0 && media.subItems().getMediaAt(0) != null) {
            mediaPlayer?.media = media.subItems().getMediaAt(0)
        } else {
            mediaPlayer?.media = media
        }

        // Options for network playback only
        media.addOption(":network-caching=1000")
        media.addOption(":clock-jitter=0")
        media.addOption(":clock-synchro=0")
        // Enable hardware decoding
        media.setHWDecoderEnabled(true, false)
        media.release()
        mediaPlayer?.play()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnSubtitles -> {
                val popUp = PopupMenu(this, binding.btnSubtitles)

                playerViewModel.subtitleTracks.forEachIndexed { index, subtitleTrack ->
                    val description = if (subtitleTrack.description.isNullOrBlank()) {
                        subtitleTrack.language.getSubtitleDescription()
                    } else {
                        getString(R.string.subtitle_description, subtitleTrack.description, subtitleTrack.language.getSubtitleDescription())
                    }
                    val menuItem = popUp.menu.add(0, index, index, description)
                    menuItem.isCheckable = true
                    menuItem.isChecked = index == playerViewModel.selectedSubtitleTrack
                }

                popUp.setOnMenuItemClickListener {
                    val currentStatus = it.isChecked
                    it.isChecked = !currentStatus
                    if (it.isChecked) {
                        subtitleSelected(it.itemId)
                    } else {
                        subtitleSelected(null)
                    }
                    true
                }
                popUp.show()
            }
            binding.btnAudioTrack -> {
                val popUp = PopupMenu(this, binding.btnAudioTrack)

                playerViewModel.audioTracks.forEachIndexed { index, audioTrack ->
                    val description = if (audioTrack.description.isNullOrBlank()) {
                        audioTrack.language.getSubtitleDescription()
                    } else {
                        getString(R.string.audio_description, audioTrack.description, audioTrack.language.getSubtitleDescription())
                    }
                    val menuItem = popUp.menu.add(0, index, index, description)
                    menuItem.isCheckable = true
                    menuItem.isChecked = index == playerViewModel.selectedAudioTrack
                }

                popUp.setOnMenuItemClickListener {
                    val currentStatus = it.isChecked
                    it.isChecked = !currentStatus
                    if (it.isChecked) {
                        audioSelected(it.itemId)
                    } else {
                        audioSelected(null)
                    }
                    true
                }
                popUp.show()
            }
            binding.btnFitScreen -> {
                val layoutParams = binding.videoLayout.layoutParams
                layoutParams.height = 200
                layoutParams.width = 200
                binding.videoLayout.layoutParams = layoutParams
            }
            else -> {

            }
        }
    }
}
