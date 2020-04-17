package kr.petworld.petworld

import android.app.Application

import com.facebook.stetho.Stetho

class PetWorldApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}