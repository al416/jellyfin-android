package org.jellyfin.client.android.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jellyfin.client.android.domain.models.Resource
import org.jellyfin.client.android.domain.models.VideoPlaybackInformation
import org.jellyfin.client.android.domain.usecase.GetVideoPlaybackInformation
import org.videolan.libvlc.interfaces.IMedia
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class PlayerViewModel @Inject constructor(@Named("computation") private val computationDispatcher: CoroutineDispatcher,
                                          private val getVideoPlaybackInformation: GetVideoPlaybackInformation
) : ViewModel() {

    private lateinit var mediaId: UUID
    var orientationLocked = false
    var selectedSubtitleTrack: Int? = null
    val subtitleTracks = mutableListOf<IMedia.SubtitleTrack>()
    lateinit var url: String
    var currentPosition: Long = 0

    fun initialize(mediaId: String) {
        this.mediaId = UUID.fromString(mediaId)
    }

    private val videoPlaybackInformation: MutableLiveData<Resource<VideoPlaybackInformation>> by lazy {
        val result = MutableLiveData<Resource<VideoPlaybackInformation>>()
        loadVideoPlaybackInformation(result)
        result
    }

    fun getVideoPlaybackInformation(): LiveData<Resource<VideoPlaybackInformation>> = videoPlaybackInformation

    private fun loadVideoPlaybackInformation(data: MutableLiveData<Resource<VideoPlaybackInformation>>) {
        viewModelScope.launch(computationDispatcher) {
            getVideoPlaybackInformation.invoke(GetVideoPlaybackInformation.RequestParams(mediaId)).collectLatest {
                data.postValue(it)
            }
        }
    }
}
