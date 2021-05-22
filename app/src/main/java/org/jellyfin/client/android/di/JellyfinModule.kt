package org.jellyfin.client.android.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import org.jellyfin.client.android.domain.constants.CodecTypes
import org.jellyfin.client.android.domain.constants.ContainerTypes
import org.jellyfin.sdk.Jellyfin
import org.jellyfin.sdk.api.client.HttpClientOptions
import org.jellyfin.sdk.api.client.KtorClient
import org.jellyfin.sdk.api.operations.ImageApi
import org.jellyfin.sdk.api.operations.ItemsApi
import org.jellyfin.sdk.api.operations.MediaInfoApi
import org.jellyfin.sdk.api.operations.TvShowsApi
import org.jellyfin.sdk.api.operations.UserApi
import org.jellyfin.sdk.api.operations.UserLibraryApi
import org.jellyfin.sdk.api.operations.UserViewsApi
import org.jellyfin.sdk.api.operations.VideoHlsApi
import org.jellyfin.sdk.api.operations.VideosApi
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.DeviceInfo
import org.jellyfin.sdk.model.api.CodecProfile
import org.jellyfin.sdk.model.api.CodecType
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.DirectPlayProfile
import org.jellyfin.sdk.model.api.DlnaProfileType
import org.jellyfin.sdk.model.api.EncodingContext
import org.jellyfin.sdk.model.api.ProfileCondition
import org.jellyfin.sdk.model.api.ProfileConditionType
import org.jellyfin.sdk.model.api.ProfileConditionValue
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import org.jellyfin.sdk.model.api.SubtitleProfile
import org.jellyfin.sdk.model.api.TranscodeSeekInfo
import org.jellyfin.sdk.model.api.TranscodingProfile
import java.util.*
import javax.inject.Singleton

@Module
@Suppress("unused")
object JellyfinModule {

    // TODO: Set these values to reasonable levels once testing is complete
    private const val CONNECTION_TIMEOUT_IN_MS = 60000L
    private const val SOCKET_TIMEOUT_IN_MS = 60000L
    private const val REQUEST_TIMEOUT_IN_MS = 80000L

    @Singleton
    @Provides
    internal fun providesJellyfin(): Jellyfin {
        return Jellyfin {
            // TODO: Set correct client and device info
            clientInfo = ClientInfo(name = "Jellyfin Web", version = "10.7.1")
            deviceInfo = DeviceInfo(id = UUID.randomUUID().toString(), name = "Firefox")
        }
    }

    @Singleton
    @Provides
    internal fun providesKtorClient(jellyfin: Jellyfin): KtorClient {
        val clientOptions = HttpClientOptions(followRedirects = true,
            connectTimeout = CONNECTION_TIMEOUT_IN_MS,
            socketTimeout = SOCKET_TIMEOUT_IN_MS,
            requestTimeout = REQUEST_TIMEOUT_IN_MS)
        return jellyfin.createApi(baseUrl = null, httpClientOptions = clientOptions)
    }

    @Provides
    @Reusable
    internal fun providesUserApi(api: KtorClient): UserApi {
        return UserApi(api)
    }

    @Provides
    @Reusable
    internal fun providesUserViewsApi(api: KtorClient): UserViewsApi {
        return UserViewsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesItemsApi(api: KtorClient): ItemsApi {
        return ItemsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesTvShowsApi(api: KtorClient): TvShowsApi {
        return TvShowsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesUserLibraryApi(api: KtorClient): UserLibraryApi {
        return UserLibraryApi(api)
    }

    @Provides
    @Reusable
    internal fun providesImageApi(api: KtorClient): ImageApi {
        return ImageApi(api)
    }

    @Provides
    @Reusable
    internal fun providesVideoHlsApi(api: KtorClient): VideoHlsApi {
        return VideoHlsApi(api)
    }

    @Provides
    @Reusable
    internal fun providesVideosApi(api: KtorClient): VideosApi {
        return VideosApi(api)
    }

    @Provides
    @Reusable
    internal fun providesMediaInfoApi(api: KtorClient): MediaInfoApi {
        return MediaInfoApi(api)
    }

    @Provides
    @Reusable
    internal fun providesCodecProfiles(): List<CodecProfile> {
        val videoCodecProfile = CodecProfile(CodecType.VIDEO, codec = CodecTypes.H264, conditions = listOf(
            ProfileCondition(
                ProfileConditionType.EQUALS_ANY,
                isRequired = true,
                property = ProfileConditionValue.VIDEO_PROFILE,
                value = "high|main|baseline|constrained baseline"
            ),
            ProfileCondition(
                ProfileConditionType.LESS_THAN_EQUAL,
                isRequired = true,
                property = ProfileConditionValue.VIDEO_LEVEL,
                value = "51" // TODO: return 41 if fire stick
            )
        ))

        val refFramesProfile = CodecProfile(CodecType.VIDEO, codec = CodecTypes.H264, conditions = listOf(
            ProfileCondition(
                ProfileConditionType.LESS_THAN_EQUAL, ProfileConditionValue.REF_FRAMES, "12", isRequired = true
            ),
        ), applyConditions = listOf(
            ProfileCondition(
                ProfileConditionType.GREATER_THAN_EQUAL, ProfileConditionValue.WIDTH, "1200", isRequired = false
            )
        ))

        val refFramesProfile2 = CodecProfile(CodecType.VIDEO, codec = CodecTypes.H264, conditions = listOf(
            ProfileCondition(
                ProfileConditionType.LESS_THAN_EQUAL, ProfileConditionValue.REF_FRAMES, "2", isRequired = true
            ),
        ), applyConditions = listOf(
            ProfileCondition(
                ProfileConditionType.GREATER_THAN_EQUAL, ProfileConditionValue.WIDTH, "1900", isRequired = false
            )
        ))

        val videoAudioCodecProfile = CodecProfile(CodecType.VIDEO_AUDIO, codec = null, conditions = listOf(
            ProfileCondition(
                ProfileConditionType.LESS_THAN_EQUAL, ProfileConditionValue.AUDIO_CHANNELS, "6", isRequired = true
            )
        ))

        // TODO: Fix this profile (need to check if device supports HEVC and 10 bit or not
        // see here: https://github.com/jellyfin/jellyfin-androidtv/blob/bef42e8f54176301348fa79b289f8976ff01c45b/app/src/main/java/org/jellyfin/androidtv/util/ProfileHelper.java
        val hevcProfile = CodecProfile(CodecType.VIDEO, codec = CodecTypes.HEVC, conditions = listOf(
            ProfileCondition(
                ProfileConditionType.LESS_THAN_EQUAL, ProfileConditionValue.AUDIO_CHANNELS, "6", isRequired = true
            )
        ))

        return listOf(videoCodecProfile, refFramesProfile, refFramesProfile2, videoAudioCodecProfile, hevcProfile)
    }

    @Provides
    @Reusable
    internal fun providesDirectPlayProfiles(): List<DirectPlayProfile> {
        val containers = mutableListOf<String>()
        val isLiveTv = true // TODO: Use a proper device check for this bool
        if (isLiveTv) {
            containers.add(ContainerTypes.TS);
            containers.add(ContainerTypes.MPEGTS);
        }
        containers.addAll(
            listOf(
                ContainerTypes.M4V,
                ContainerTypes.MOV,
                ContainerTypes.XVID,
                ContainerTypes.VOB,
                ContainerTypes.MKV,
                ContainerTypes.WMV,
                ContainerTypes.ASF,
                ContainerTypes.OGM,
                ContainerTypes.OGV,
                ContainerTypes.MP4,
                ContainerTypes.WEBM
            )
        );

        val videoCodecs = mutableListOf<String>()
        videoCodecs.addAll(
            listOf(
                CodecTypes.H264,
                CodecTypes.HEVC,
                CodecTypes.VP8,
                CodecTypes.VP9,
                ContainerTypes.MPEG,
                CodecTypes.MPEG2VIDEO
            )
        )

        val audioCodecs = mutableListOf<String>()
        audioCodecs.addAll(
            listOf(
                CodecTypes.AAC,
                CodecTypes.AC3,
                CodecTypes.EAC3,
                CodecTypes.AAC_LATM,
                CodecTypes.MP3,
                CodecTypes.MP2
            )
        )

        val allowDTS = true // TODO: fix later by figuring out if the device supports it
        if (allowDTS) {
            audioCodecs.add(CodecTypes.DCA);
            audioCodecs.add(CodecTypes.DTS);
        }

        val videoDirectPlayProfile = DirectPlayProfile(container = containers.joinToString(","), videoCodec = videoCodecs.joinToString(","),
            audioCodec = audioCodecs.joinToString(","), type = DlnaProfileType.VIDEO)

        val audioContainers = mutableListOf<String>()
        audioContainers.addAll(
            listOf(
                CodecTypes.AAC,
                CodecTypes.MP3,
                CodecTypes.MPA,
                CodecTypes.WAV,
                CodecTypes.WMA,
                CodecTypes.MP2,
                ContainerTypes.OGG,
                ContainerTypes.OGA,
                ContainerTypes.WEBMA,
                CodecTypes.APE,
                CodecTypes.OPUS
            )
        )

        val audioDirectPlayProfile = DirectPlayProfile(container = audioContainers.joinToString(","), videoCodec = null,
            audioCodec = null, type = DlnaProfileType.AUDIO)

        val photoDirectPlayProfile = DirectPlayProfile(container = "jpg,jpeg,png,gif", audioCodec = null, videoCodec = null, type = DlnaProfileType.PHOTO)

        return listOf(videoDirectPlayProfile, audioDirectPlayProfile, photoDirectPlayProfile)
    }

    @Provides
    @Reusable
    internal fun providesTranscodingProfiles(): List<TranscodingProfile> {
        return listOf(
            TranscodingProfile(
                audioCodec = "aac,mp3,opus",
                breakOnNonKeyFrames = true,
                container = "ts",
                context = EncodingContext.STREAMING,
                maxAudioChannels = "2",
                minSegments = 1,
                protocol = "hls",
                type = DlnaProfileType.VIDEO,
                videoCodec = "h264",
                copyTimestamps = false,
                enableMpegtsM2TsMode = true,
                enableSubtitlesInManifest = true,
                estimateContentLength = true,
                segmentLength = 10,
                transcodeSeekInfo = TranscodeSeekInfo.AUTO
            )
        )
    }

    @Provides
    @Reusable
    internal fun providesSubtitleProfile(): List<SubtitleProfile> {
        val result = mutableListOf<SubtitleProfile>()
        result.add(SubtitleProfile("srt", SubtitleDeliveryMethod.EXTERNAL))
        result.add(SubtitleProfile("srt", SubtitleDeliveryMethod.EMBED))
        result.add(SubtitleProfile("subrip", SubtitleDeliveryMethod.EMBED))
        result.add(SubtitleProfile("ass", SubtitleDeliveryMethod.ENCODE))
        result.add(SubtitleProfile("ssa", SubtitleDeliveryMethod.ENCODE))
        result.add(SubtitleProfile("pgs", SubtitleDeliveryMethod.ENCODE))
        result.add(SubtitleProfile("pgssub", SubtitleDeliveryMethod.ENCODE))
        result.add(SubtitleProfile("dvdsub", SubtitleDeliveryMethod.EMBED))
        result.add(SubtitleProfile("vtt", SubtitleDeliveryMethod.EMBED))
        result.add(SubtitleProfile("sub", SubtitleDeliveryMethod.EMBED))
        result.add(SubtitleProfile("idx", SubtitleDeliveryMethod.EMBED))
        return result
    }

    @Provides
    @Reusable
    internal fun providesDeviceProfile(
        codecProfiles: List<CodecProfile>,
        directPlayProfiles: List<DirectPlayProfile>,
        transcodingProfiles: List<TranscodingProfile>,
        subtitleProfiles: List<SubtitleProfile>
    ): DeviceProfile {

        return DeviceProfile(
            name = "Android",
            maxStreamingBitrate = 20000000,
            maxStaticBitrate = 100000000,
            codecProfiles = codecProfiles,
            directPlayProfiles = directPlayProfiles,
            transcodingProfiles = transcodingProfiles,
            subtitleProfiles = subtitleProfiles,
            requiresPlainVideoItems = false,
            requiresPlainFolders = false,
            enableAlbumArtInDidl = false,
            enableMsMediaReceiverRegistrar = false,
            enableSingleAlbumArtLimit = false,
            enableSingleSubtitleLimit = false,
            ignoreTranscodeByteRangeRequests = false,
            maxAlbumArtHeight = 100,
            maxAlbumArtWidth = 100,
            timelineOffsetSeconds = 1000
        )
    }
}