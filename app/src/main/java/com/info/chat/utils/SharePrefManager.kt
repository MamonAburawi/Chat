package com.info.chat.utils

import android.content.Context
import com.google.gson.Gson
import com.info.chat.data.User

class SharePrefManager(context: Context) {

    private var userSession = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    private var editor = userSession.edit()



    fun saveUser(user: User){
        val gson = Gson()
        val json = gson.toJson(user)
        editor.putString(KEY_USER,json)
        editor.commit()
    }


    fun loadUser(): User? {
        val gson = Gson()
        val json: String? = userSession.getString(LOGGED_USER, null)
        return gson.fromJson(json, User::class.java)
    }


    fun signOut() {
        editor.clear()
        editor.commit()
    }

    companion object {
        private const val KEY_USER = "user"
    }


}