//package com.lensnap.app.data
//
//import android.util.Log
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.ChildEventListener
//import com.lensnap.app.models.Answer
//import com.lensnap.app.models.IceCandidateModel
//import com.lensnap.app.models.Offer
//import org.webrtc.IceCandidate
//import org.webrtc.SessionDescription
//
//class FirebaseSignaling {
//    private val database = FirebaseDatabase.getInstance()
//
//    fun sendOffer(offer: SessionDescription, receiverId: String) {
//        val offerMap = hashMapOf(
//            "type" to offer.type.canonicalForm(),
//            "sdp" to offer.description,
//            "receiverId" to receiverId
//        )
//        database.getReference("offers").push().setValue(offerMap)
//    }
//
//    fun sendAnswer(answer: SessionDescription) {
//        val answerMap = hashMapOf(
//            "type" to answer.type.canonicalForm(),
//            "sdp" to answer.description
//        )
//        database.getReference("answers").push().setValue(answerMap)
//    }
//
//    fun sendIceCandidate(candidate: IceCandidate) {
//        val candidateMap = hashMapOf(
//            "sdpMid" to candidate.sdpMid,
//            "sdpMLineIndex" to candidate.sdpMLineIndex,
//            "candidate" to candidate.sdp
//        )
//        database.getReference("iceCandidates").push().setValue(candidateMap)
//    }
//
//    fun sendSessionDescription(sessionDescription: SessionDescription) {
//        val descriptionMap = hashMapOf(
//            "type" to sessionDescription.type.canonicalForm(),
//            "sdp" to sessionDescription.description
//        )
//        database.getReference("sessionDescriptions").push().setValue(descriptionMap)
//    }
//
//    fun receiveOffer(callback: (SessionDescription, String) -> Unit) {
//        database.getReference("offers").addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val offer = snapshot.getValue(Offer::class.java)
//                offer?.let {
//                    val sessionDescription = SessionDescription(SessionDescription.Type.OFFER, it.sdp)
//                    Log.d("FirebaseSignaling", "Incoming offer received for receiverId: ${it.receiverId}")
//                    callback(sessionDescription, it.receiverId)
//                }
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("FirebaseSignaling", "Error receiving offer: ${error.message}")
//            }
//        })
//    }
//
//    fun receiveAnswer(callback: (SessionDescription) -> Unit) {
//        database.getReference("answers").addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val answer = snapshot.getValue(Answer::class.java)
//                answer?.let {
//                    val sessionDescription = SessionDescription(SessionDescription.Type.ANSWER, it.sdp)
//                    Log.d("FirebaseSignaling", "Incoming answer received")
//                    callback(sessionDescription)
//                }
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("FirebaseSignaling", "Error receiving answer: ${error.message}")
//            }
//        })
//    }
//
//    fun receiveIceCandidate(callback: (IceCandidate) -> Unit) {
//        database.getReference("iceCandidates").addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val iceCandidate = snapshot.getValue(IceCandidateModel::class.java)
//                iceCandidate?.let {
//                    val candidate = IceCandidate(it.sdpMid, it.sdpMLineIndex, it.candidate)
//                    Log.d("FirebaseSignaling", "Incoming ICE candidate received: ${candidate.sdp}")
//                    callback(candidate)
//                }
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("FirebaseSignaling", "Error receiving ICE candidate: ${error.message}")
//            }
//        })
//    }
//
//    fun listenForIncomingCalls(callback: (String) -> Unit) {
//        database.getReference("offers").addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val offer = snapshot.getValue(Offer::class.java)
//                offer?.let {
//                    Log.d("FirebaseSignaling", "Incoming call detected for receiverId: ${it.receiverId}")
//                    callback(it.receiverId)
//                }
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("FirebaseSignaling", "Error receiving incoming call: ${error.message}")
//            }
//        })
//    }
//}
