package com.treinetic.whiteshark.roomdb

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.roomdb.models.CartItemData
import com.treinetic.whiteshark.roomdb.models.DeviceInfo

@Keep
@Dao
interface DeviceInfoDao {

    @Insert
    fun save(vararg book: DeviceInfo)

    @Query("SELECT * FROM DeviceInfo WHERE id=(:id)")
    fun getCartItem(id: String): List<DeviceInfo>

    @Query("DELETE FROM DeviceInfo")
    fun deleteAll()

    @Query("SELECT * FROM DeviceInfo")
    fun getAll(): List<DeviceInfo>
}