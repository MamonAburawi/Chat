package com.info.chat.screens.incoming_requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.info.chat.utils.FirestoreUtil
import com.info.chat.data.User
import com.info.chat.screens.different_user_profile.RECEIVED_REQUEST_ARRAY
import com.info.chat.screens.different_user_profile.SENT_REQUEST_ARRAY
import com.google.firebase.firestore.FieldValue

const val FRIENDS = "friends"


class IncomingRequestsViewModel : ViewModel() {

    private val usersRef = FirestoreUtil.firestoreInstance.collection("users")
    private val friendRequestersMutableLiveData = MutableLiveData<MutableList<User>?>()


    //get info of the users that sent friend requests
    fun downloadRequests(receivedRequests: List<String>): LiveData<MutableList<User>?> {

        val friendRequesters = mutableListOf<User>()

        for (receivedRequest in receivedRequests) {
            usersRef.document(receivedRequest).get().addOnSuccessListener {
                val user = it?.toObject(User::class.java)
                user?.let { it1 -> friendRequesters.add(it1) }
                friendRequestersMutableLiveData.value = friendRequesters

            }.addOnFailureListener {
                friendRequestersMutableLiveData.value = null
            }
        }
        return friendRequestersMutableLiveData
    }


    fun addToFriends(
        requesterId: String,
        loggedUserId: String
    ) {

        deleteRequest(requesterId, loggedUserId)

        //add id in sentRequest array for logged in user


        FirestoreUtil.firestoreInstance.collection("users").document(requesterId)
            .update(FRIENDS, FieldValue.arrayUnion(loggedUserId)).addOnSuccessListener {
                        //add loggedInUserId in receivedRequest array for other user
                FirestoreUtil.firestoreInstance.collection("users").document(loggedUserId)
                    .update(FRIENDS, FieldValue.arrayUnion(requesterId))
                            .addOnSuccessListener {

                            }.addOnFailureListener {

                            }
                    }.addOnFailureListener {

                    }
        }




    fun deleteRequest(
        requesterId: String,
        loggedUserId: String
    ) {

        //remove id from sentRequest array for logged in user
                FirestoreUtil.firestoreInstance.collection("users").document(loggedUserId)
                    .update(RECEIVED_REQUEST_ARRAY, FieldValue.arrayRemove(requesterId))
                    .addOnSuccessListener {
                        //remove loggedInUserId from receivedRequest array for other user
                        FirestoreUtil.firestoreInstance.collection("users").document(requesterId)
                            .update(
                                SENT_REQUEST_ARRAY,
                                FieldValue.arrayRemove(loggedUserId)
                            )
                            .addOnSuccessListener {

                            }.addOnFailureListener {

                            }
                    }.addOnFailureListener {

                    }
    }

}




