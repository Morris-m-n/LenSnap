//package com.lensnap.app.data
//
//import org.webrtc.*
//import android.content.Context
//import android.util.Log
//import androidx.navigation.NavController
//import com.lensnap.app.CustomSdpObserver
//import org.webrtc.Camera2Enumerator
//import org.webrtc.EglBase
//import org.webrtc.IceCandidate
//import org.webrtc.MediaConstraints
//import org.webrtc.MediaStream
//import org.webrtc.PeerConnection
//import org.webrtc.PeerConnectionFactory
//import org.webrtc.SurfaceTextureHelper
//import org.webrtc.VideoCapturer
//import org.webrtc.VideoTrack
//
//class WebRTCManager(
//    private val context: Context,
//    private val signaling: FirebaseSignaling,
//    private val navController: NavController // Add NavController parameter
//) {
//    private val peerConnectionFactory: PeerConnectionFactory
//    private lateinit var eglBase: EglBase
//    private lateinit var localAudioTrack: AudioTrack
//    private lateinit var localVideoTrack: VideoTrack
//    private lateinit var remoteVideoTrack: VideoTrack
//    private var peerConnection: PeerConnection? = null
//
//    init {
//        PeerConnectionFactory.initialize(
//            PeerConnectionFactory.InitializationOptions.builder(context)
//                .createInitializationOptions()
//        )
//        eglBase = EglBase.create()
//        peerConnectionFactory = PeerConnectionFactory.builder()
//            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
//            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
//            .createPeerConnectionFactory()
//
//        val audioConstraints = MediaConstraints()
//        val audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
//        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)
//
//        // Set up signaling callbacks
//        signaling.receiveIceCandidate { candidate -> receiveIceCandidate(candidate) }
//        signaling.receiveOffer { offer, receiverId ->
//            Log.d("WebRTC", "Incoming call detected for receiverId: $receiverId")
//            receiveOffer(offer)
//            navigateToIncomingCallScreen(receiverId)
//        }
//        signaling.receiveAnswer { answer -> receiveAnswer(answer) }
//
//        Log.d("WebRTC", "WebRTCManager initialized")
//    }
//
//    fun createPeerConnection(): PeerConnection? {
//        if (peerConnection != null) {
//            Log.w("WebRTC", "PeerConnection already exists")
//            return peerConnection
//        }
//
//        val iceServers = listOf(
//            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
//        )
//        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
//        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
//
//        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
//            override fun onIceCandidate(candidate: IceCandidate?) {
//                candidate?.let {
//                    Log.d("WebRTC", "Sending ICE candidate: ${it.sdp}")
//                    signaling.sendIceCandidate(it)
//                }
//            }
//
//            override fun onTrack(transceiver: RtpTransceiver?) {
//                transceiver?.receiver?.track()?.let { track ->
//                    if (track.kind() == VideoTrack::class.java.canonicalName) {
//                        remoteVideoTrack = track as VideoTrack
//                        Log.d("WebRTC", "Remote video track received")
//                    }
//                }
//            }
//
//            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
//                Log.d("WebRTC", "ICE candidates removed")
//            }
//
//            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {
//                Log.d("WebRTC", "Signaling state changed to: $newState")
//            }
//
//            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
//                Log.d("WebRTC", "ICE connection state changed to: $newState")
//                if (newState == PeerConnection.IceConnectionState.DISCONNECTED || newState == PeerConnection.IceConnectionState.FAILED) {
//                    Log.e("WebRTC", "ICE connection failed or disconnected, handle reconnection here")
//                    // Handle reconnection logic
//                }
//            }
//
//            override fun onIceConnectionReceivingChange(receiving: Boolean) {
//                Log.d("WebRTC", "ICE connection receiving changed: $receiving")
//            }
//
//            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
//                Log.d("WebRTC", "ICE gathering state changed to: $newState")
//            }
//
//            override fun onRemoveStream(stream: MediaStream?) {
//                Log.d("WebRTC", "Stream removed: ${stream?.id}")
//            }
//
//            override fun onDataChannel(dc: DataChannel?) {
//                Log.d("WebRTC", "Data channel received: ${dc?.label()}")
//            }
//
//            override fun onRenegotiationNeeded() {
//                Log.d("WebRTC", "Renegotiation needed")
//            }
//
//            override fun onAddStream(stream: MediaStream?) {
//                Log.d("WebRTC", "Stream added: ${stream?.id}")
//            }
//        })
//        Log.d("WebRTC", "PeerConnection created")
//        return peerConnection
//    }
//
//    fun startAudioCall() {
//        peerConnection?.addTrack(localAudioTrack)
//        Log.d("WebRTC", "Audio call started")
//    }
//
//    fun startVideoCall() {
//        initLocalVideoTrack()
//        peerConnection?.addTrack(localAudioTrack)
//        peerConnection?.addTrack(localVideoTrack)
//        Log.d("WebRTC", "Video call started")
//    }
//
//    fun receiveOffer(offer: SessionDescription) {
//        Log.d("WebRTC", "Receiving offer: ${offer.description}")
//        peerConnection?.setRemoteDescription(CustomSdpObserver(), offer)?.let { result ->
//            Log.d("WebRTC", "Set remote description result: $result")
//            if (result != null) {
//                peerConnection?.createAnswer(object : CustomSdpObserver() {
//                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
//                        Log.d("WebRTC", "Answer created: ${sessionDescription.description}")
//                        peerConnection?.setLocalDescription(CustomSdpObserver(), sessionDescription)?.let { result ->
//                            Log.d("WebRTC", "Set local description result: $result")
//                            signaling.sendAnswer(sessionDescription)
//                        }
//                    }
//                }, MediaConstraints())
//            }
//        } ?: Log.e("WebRTC", "Failed to set remote description")
//    }
//
//    fun receiveAnswer(answer: SessionDescription) {
//        Log.d("WebRTC", "Receiving answer: ${answer.description}")
//        peerConnection?.setRemoteDescription(CustomSdpObserver(), answer)?.let { result ->
//            Log.d("WebRTC", "Set remote description result: $result")
//        } ?: Log.e("WebRTC", "Failed to set remote description")
//    }
//
//    fun receiveIceCandidate(candidate: IceCandidate) {
//        Log.d("WebRTC", "Receiving ICE candidate: ${candidate.sdp}")
//        peerConnection?.addIceCandidate(candidate)?.let { result ->
//            Log.d("WebRTC", "Add ICE candidate result: $result")
//        } ?: Log.e("WebRTC", "Failed to add ICE candidate")
//    }
//
//    private fun initLocalVideoTrack() {
//        val videoCapturer: VideoCapturer = createVideoCapturer() ?: return
//        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
//        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
//        videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
//        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)
//        localVideoTrack.setEnabled(true)
//        Log.d("WebRTC", "Local video track initialized")
//    }
//
//    private fun createVideoCapturer(): VideoCapturer? {
//        val enumerator = Camera2Enumerator(context)
//        enumerator.deviceNames.forEach { deviceName ->
//            if (enumerator.isFrontFacing(deviceName)) {
//                return enumerator.createCapturer(deviceName, null)
//            }
//        }
//        Log.e("WebRTC", "Failed to create video capturer")
//        return null
//    }
//
//    private fun navigateToIncomingCallScreen(receiverId: String) {
//        // Navigation logic to go to the incoming call screen
//        Log.d("WebRTC", "Navigating to incoming call screen for receiverId: $receiverId")
//        // Example: navController.navigate(R.id.action_currentFragment_to_incomingCallFragment, bundleOf("receiverId" to receiverId))
//    }
//
//    fun endCall() {
//        peerConnection?.close()
//        peerConnection = null
//        Log.d("WebRTC", "Call ended and PeerConnection closed")
//    }
//}
