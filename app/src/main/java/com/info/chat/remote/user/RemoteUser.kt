package com.info.chat.remote.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.info.chat.data.User
import com.info.chat.screens.incoming_requests.FRIENDS
import com.info.chat.utils.AuthUtil
import com.info.chat.utils.FirestoreUtil
import com.info.chat.utils.findDiffElements

class RemoteUser {

    companion object {
        const val TAG = "RemoteUser"
        const val USERS_FIELD = "users"
        const  val FRIENDS = "friends"
    }

    private val _root = FirebaseFirestore.getInstance()
    private val _auth = FirebaseAuth.getInstance()

    private fun usersCollections() = _root.collection(USERS_FIELD)
    private fun userId() = _auth.currentUser?.uid!!

    val usersLiveData = MutableLiveData<MutableList<User?>>()




    suspend fun loadUsers(onComplete: (List<User>) -> Unit) {

        // remove current user from result
        usersCollections().get().addOnSuccessListener {
            if (it != null){
                val users = it.toObjects(User::class.java)
                val r = users.filter { it.uid != userId() }.toMutableList()


                // remove user friends from result
                usersCollections().whereArrayContains(FRIENDS, userId())
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        if (firebaseFirestoreException == null) {
                            val friends = querySnapshot?.toObjects(User::class.java)
                            if (friends != null) {
                                r.removeAll(friends)
                                onComplete(r)
                            }
                        }
                    }

            }
        }

    }
}





