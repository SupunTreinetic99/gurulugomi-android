package com.treinetic.whiteshark.fragments.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.treinetic.whiteshark.exceptions.NetException
import com.treinetic.whiteshark.models.User
import com.treinetic.whiteshark.services.Service
import com.treinetic.whiteshark.services.UserService

class RegisterViewModel : ViewModel() {

    private var registerUser: MutableLiveData<User> = MutableLiveData()
    private var netException: MutableLiveData<NetException> = MutableLiveData()

    fun getUser(): MutableLiveData<User> {
        return registerUser
    }

    fun clearException() {
        netException.value = null
    }

    fun getNetException(): MutableLiveData<NetException> = netException

    fun registerUser(user: User) {
        UserService.getInstance().registerUser(user, { result ->
            registerUser.value = result
        }, { exception ->
            exception.printStackTrace()
            netException.value = exception
        })
    }


}