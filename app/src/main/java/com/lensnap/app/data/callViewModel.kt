//package com.lensnap.app.data
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.launch
//import org.webrtc.PeerConnection
//import android.util.Log
//
//class CallViewModel(private val context: Context, private val signaling: FirebaseSignaling) : ViewModel() {
//
//    private val webRTCManager: WebRTCManager = WebRTCManager(context, signaling, nav)
//    private var peerConnection: PeerConnection? = null
//
//    init {
//        peerConnection = webRTCManager.createPeerConnection()
//        if (peerConnection == null) {
//            Log.e("CallViewModel", "Failed to create PeerConnection")
//        }
//    }
//
//    fun startWebRTCCall(isVideoCall: Boolean) {
//        peerConnection?.let {
//            viewModelScope.launch {
//                if (isVideoCall) {
//                    webRTCManager.startVideoCall()
//                } else {
//                    webRTCManager.startAudioCall()
//                }
//            }
//        } ?: Log.e("CallViewModel", "PeerConnection is null")
//    }
//
//    fun handleRejectCall() {
//        // Logic to handle call rejection
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        // Clean up the peer connection when ViewModel is cleared
//        peerConnection?.dispose()
//    }
//}
