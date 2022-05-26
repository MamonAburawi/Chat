package com.info.chat.repository

import com.info.chat.data.User
import com.info.chat.remote.user.RemoteUser

class UserRepository(private val remoteUser: RemoteUser) {

    suspend fun loadUsers(onComplete:(List<User>) -> Unit) = remoteUser.loadUsers(onComplete)



}