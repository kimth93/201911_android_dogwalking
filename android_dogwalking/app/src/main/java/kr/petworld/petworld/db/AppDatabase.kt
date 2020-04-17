package kr.petworld.petworld.db


import android.content.Context
import android.util.Log

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import kr.petworld.petworld.model.Locations
import kr.petworld.petworld.model.Walks


@Database(entities = [Locations::class, Walks::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val databaseDao: AppDao

    companion object {
        private val LOG_TAG = AppDatabase::class.java.simpleName
        private val LOCK = Any()
        private val DATABASE_NAME = "petworld"
        private var sInstance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (sInstance == null) {
                synchronized(LOCK) {
                    Log.d(LOG_TAG, "Creating new database instance")
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, AppDatabase.DATABASE_NAME
                    )
                        .allowMainThreadQueries()
                        .build()
                }
            }
            Log.d(LOG_TAG, "Getting the database instance")
            return sInstance!!
        }
    }
}
