package com.example.swipy.ui.viewmodels

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted = _isPermissionGranted.asStateFlow()

    private val _currentTrack = MutableStateFlow("Pilih Musik")
    val currentTrack = _currentTrack.asStateFlow()

    private val _currentArtist = MutableStateFlow("Menemani sesi swipe kamu")
    val currentArtist = _currentArtist.asStateFlow()

    private val _musicSource = MutableStateFlow("None") // "Local", "Spotify", "None"
    val musicSource = _musicSource.asStateFlow()

    private val mediaSessionManager = context.getSystemService("media_session") as MediaSessionManager
    private var activeController: MediaController? = null
    private var mediaCallback: MediaController.Callback? = null

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers)
    }

    init {
        refreshSessions()
    }

    fun refreshSessions() {
        try {
            val componentName = ComponentName(context, NotificationListener::class.java)
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener)
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, componentName)
            updateActiveController(mediaSessionManager.getActiveSessions(componentName))
            _isPermissionGranted.value = true
        } catch (e: SecurityException) {
            _isPermissionGranted.value = false
            // Permission not granted yet
            _musicSource.value = "None"
            _currentTrack.value = "Pilih Musik"
            _currentArtist.value = "Menemani sesi swipe kamu"
            _isPlaying.value = false
        }
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        // Prefer Spotify if multiple sessions exist, otherwise take the first active one
        val newController = controllers?.find { it.packageName.contains("spotify", ignoreCase = true) } 
            ?: controllers?.firstOrNull()

        if (activeController == newController) return

        mediaCallback?.let { activeController?.unregisterCallback(it) }
        activeController = newController

        activeController?.let { controller ->
            // Sync initial state
            updateMetadata(controller.metadata)
            updatePlaybackState(controller.playbackState)
            
            val callback = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    updateMetadata(metadata)
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    updatePlaybackState(state)
                }
            }
            mediaCallback = callback
            
            // Listen for changes
            controller.registerCallback(callback, Handler(Looper.getMainLooper()))
            
            _musicSource.value = if (controller.packageName.contains("spotify", ignoreCase = true)) "Spotify" else "Local"
        } ?: run {
            _musicSource.value = "None"
            _currentTrack.value = "Pilih Musik"
            _currentArtist.value = "Menemani sesi swipe kamu"
            _isPlaying.value = false
        }
    }

    private fun updateMetadata(metadata: MediaMetadata?) {
        _currentTrack.value = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Musik Aktif"
        _currentArtist.value = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Sedang memutar"
    }

    private fun updatePlaybackState(state: PlaybackState?) {
        _isPlaying.value = state?.state == PlaybackState.STATE_PLAYING
    }

    fun togglePlayPause() {
        activeController?.transportControls?.let {
            if (_isPlaying.value) it.pause() else it.play()
        }
    }

    fun nextTrack() {
        activeController?.transportControls?.skipToNext()
    }

    fun previousTrack() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun openSpotify() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:track:0"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }



    fun openLocalMusic() {
        val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC)
        val chooser = Intent.createChooser(intent, "Pilih Pemutar Musik Lokal")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback
            val fallback = Intent(Intent.ACTION_VIEW)
            fallback.setDataAndType(Uri.EMPTY, "audio/*")
            val fallbackChooser = Intent.createChooser(fallback, "Pilih Pemutar Musik Lokal")
            fallbackChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(fallbackChooser)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    
    fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onCleared() {
        super.onCleared()
        mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener)
    }
}

// Minimal Service required for MediaSession permissions
class NotificationListener : android.service.notification.NotificationListenerService()

