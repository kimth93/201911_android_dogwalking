package kr.petworld.petworld

import android.content.Context
import android.content.SharedPreferences

import androidx.annotation.VisibleForTesting

import com.google.gson.reflect.TypeToken

// 이름 이미지 성별 생일 견종 저장
class PreferencesManager(context: Context) {
    private val mPref: SharedPreferences


    enum class Key {
        mainImagePath, dogName, gender, birthday, dogDescription, registered
    }


    init {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setValue(key: Key, value: Any) {
        if (value.javaClass == String::class.java) {
            mPref.edit().putString(key.name, value as String).apply()
        } else if (value.javaClass == Int::class.java) {
            mPref.edit().putInt(key.name, value as Int).apply()
        } else if (value.javaClass == Float::class.java) {
            mPref.edit().putFloat(key.name, value as Float).apply()
        } else if (value.javaClass == Boolean::class.java) {
            mPref.edit().putBoolean(key.name, value as Boolean).apply()
        } else if (value.javaClass == Long::class.java) {
            mPref.edit().putLong(key.name, value as Long).apply()
        } else if (value.javaClass == object : TypeToken<Set<String>>() {

            }.javaClass) {
            mPref.edit().putStringSet(key.name, value as Set<String>).apply()
        } else {
            assert(true)
        }
    }

    fun <T> getValue(aClass: Class<T>, key: Key, defaultValue: Any): Any? {

        if (aClass == String::class.java) {
            return mPref.getString(key.name, defaultValue as String)
        } else if (aClass == Int::class.java) {
            return mPref.getInt(key.name, defaultValue as Int)
        } else if (aClass == Float::class.java) {
            return mPref.getFloat(key.name, defaultValue as Float)
        } else if (aClass == Boolean::class.java) {
            return mPref.getBoolean(key.name, defaultValue as Boolean)
        } else if (aClass == Long::class.java) {
            return mPref.getLong(key.name, defaultValue as Long)
        } else if (aClass == object : TypeToken<Set<String>>() {

            }.javaClass) {
            return mPref.getStringSet(key.name, defaultValue as Set<String>)
        } else {
            assert(true)
            return null
        }
    }

    fun remove(key: Key) {
        mPref.edit().remove(key.name).apply()
    }

    fun clear(): Boolean {
        return mPref.edit()
            .clear()
            .commit()
    }

    companion object {

        @VisibleForTesting
        val PREF_NAME = BuildConfig.APPLICATION_ID + ".local"

        private var sInstance: PreferencesManager? = null


        @Synchronized
        fun getInstance(context: Context): PreferencesManager {
            if (sInstance == null) {
                sInstance = PreferencesManager(context)
            }


            return sInstance!!
        }
    }

}