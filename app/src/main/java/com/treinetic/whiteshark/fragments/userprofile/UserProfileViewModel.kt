package com.treinetic.whiteshark.fragments.userprofile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.User
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UserService
import java.io.File


class UserProfileViewModel : ViewModel() {


    private var user: MutableLiveData<User> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()

    fun getUser(): MutableLiveData<User> {
        return user
    }

    fun clearError() {
        netException.value = null
    }

    fun getNetException(): MutableLiveData<NetException> {
        return netException
    }

    fun getInitUser() {
        user.value = UserService.getInstance().getUser()
    }

    fun updateUserImage(image: File) {
        UserService.getInstance().updateUser(image, { result ->
            user.value = result
        }, { exception ->
            netException.value = exception
        })
    }


}