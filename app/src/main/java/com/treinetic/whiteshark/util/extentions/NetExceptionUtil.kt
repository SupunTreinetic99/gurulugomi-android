package com.treinetic.whiteshark.util.extentions

import androidx.lifecycle.MutableLiveData
import com.treinetic.whiteshark.exceptions.NetException

fun MutableLiveData<NetException>.clear(){
    this.value=null
}