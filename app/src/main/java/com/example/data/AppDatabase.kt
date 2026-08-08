package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.LevelStatDao
import com.example.data.dao.UserDao
import com.example.data.model.LevelStatEntity
import com.example.data.model.UserEntity

@Database(
    entities = [UserEntity::class, LevelStatEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun levelStatDao(): LevelStatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "color_rush_3d_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
