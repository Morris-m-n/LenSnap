package com.lensnap.app.models

data class Offer(
    val sdp: String = "",
    val receiverId: String = ""
)

data class Answer(
    val sdp: String = "",
    val type: String = "answer"
)

data class IceCandidateModel(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = ""
)
