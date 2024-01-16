package com.treinetic.whiteshark.roomdb.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity

@Keep
@Entity(tableName = "DeviceInfo", primaryKeys = arrayOf("id"))
class DeviceInfo (
    @ColumnInfo(name = "id")
    var id: String,

    @ColumnInfo(name = "dif")
    var dif: String
){


}