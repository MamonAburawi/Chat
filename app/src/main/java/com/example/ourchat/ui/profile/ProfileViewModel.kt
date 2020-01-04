package com.example.ourchat.ui.profile

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.example.ourchat.R
import com.example.ourchat.Utils.LoadState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore


class ProfileViewModel(val app: Application) : AndroidViewModel(app) {

    private var usersCollectionRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("users")
    private var userDocRef: DocumentReference =
        usersCollectionRef.document(FirebaseAuth.getInstance().uid!!)

    var bioLoadState = MutableLiveData<LoadState>()
    var friendsList = MutableLiveData<List<com.example.ourchat.data.model.User>>()
    var profileImageLoadState = MutableLiveData<LoadState>()
    var loadedImage = MutableLiveData<RequestBuilder<Drawable>>()
    var bio = MutableLiveData<String>()


    fun updateBio(bio: String) {

        bioLoadState.value = LoadState.LOADING

        userDocRef.update("bio", bio)
            .addOnSuccessListener {
                bioLoadState.value = LoadState.SUCCESS

            }
            .addOnFailureListener {
                bioLoadState.value = LoadState.FAILURE
            }

    }


    fun downloadBio() {
        bioLoadState.value = LoadState.LOADING

        userDocRef.addSnapshotListener(EventListener { document, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                bioLoadState.value = LoadState.SUCCESS
                bio.value = document?.get("bio")?.toString() ?: "No bio"
            } else {
                bioLoadState.value = LoadState.FAILURE
            }
        })

    }


    fun downloadProfileImage() {

        profileImageLoadState.value = LoadState.LOADING

        userDocRef.get()
            .addOnSuccessListener { document ->
                var profileImageUrl = document.get("profile_picture_url").toString()
                loadedImage.value = Glide.with(app).load(profileImageUrl).apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation).error(R.drawable.anonymous_profile)
                )
                profileImageLoadState.value = LoadState.SUCCESS

            }
            .addOnFailureListener { exception ->
                profileImageLoadState.value = LoadState.FAILURE
            }

    }


    fun loadFriends(): LiveData<List<com.example.ourchat.data.model.User>> {


        userDocRef.addSnapshotListener(EventListener { snapShopt, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                val user = snapShopt?.toObject(com.example.ourchat.data.model.User::class.java)
                val friendsIds = user?.friends
                if (friendsIds != null && friendsIds.isNotEmpty()) {
                    val mFriendList = mutableListOf<com.example.ourchat.data.model.User>()
                    for (friendId in friendsIds) {
                        usersCollectionRef.document(friendId).get().addOnSuccessListener {
                            val friend =
                                it.toObject(com.example.ourchat.data.model.User::class.java)
                            friend?.let { user -> mFriendList.add(user) }
                            friendsList.value = mFriendList
                        }
                    }

                } else {
                    friendsList.value = null
                }
            } else {
                friendsList.value = null
            }
        })
        return friendsList
    }
}
