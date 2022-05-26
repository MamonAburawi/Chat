package com.info.chat.screens.finduser

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.info.chat.data.User
import com.info.chat.remote.user.RemoteUser
import com.info.chat.repository.user.UserRepository
import kotlinx.coroutines.launch

class FindUserViewModel: ViewModel() {

    companion object{
        const val TAG = "FindUserViewModel"
    }

    private val userRepository = UserRepository(RemoteUser())

    var users = MutableLiveData<MutableList<User?>>()


    init {
        initData()
    }


    private fun initData(){
        viewModelScope.launch {

          userRepository.loadUsers{
                users.value = it.toMutableList()
                Log.d(TAG,"users: ${it.size}")
            }

        }
    }


}
