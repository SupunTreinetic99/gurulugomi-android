package com.treinetic.whiteshark.roomdb

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.treinetic.whiteshark.MyApp
import com.treinetic.whiteshark.roomdb.models.BookData
import com.treinetic.whiteshark.roomdb.models.CartItemData
import com.treinetic.whiteshark.roomdb.models.DeviceInfo

/**
 * Created by Nuwan on 2/28/19.
 */
@Keep
@Database(
    entities = [BookData::class, CartItemData::class, DeviceInfo::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDataDao(): BookDataDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun deviceInfoDao(): DeviceInfoDao

    companion object {

        private val db by lazy {
            val MIGRATION_1_2 = object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS `CartItems` (`bookId` TEXT NOT NULL, `book` TEXT NOT NULL, " +
                                "PRIMARY KEY(`bookId`))"
                    )
                }
            }

            val MIGRATION_2_3 = object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS `DeviceInfo` (`id` TEXT NOT NULL, `dif` TEXT NOT NULL, " +
                                "PRIMARY KEY(`id`))"
                    )
                }
            }

            Room.databaseBuilder(MyApp.getAppContext(), AppDatabase::class.java, "md-book-app")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }

        fun getInstance(): AppDatabase {
            return db
        }
    }


}