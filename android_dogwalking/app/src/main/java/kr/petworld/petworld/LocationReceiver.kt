package kr.petworld.petworld

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Parcelable
import android.util.Log

import br.com.safety.locationlistenerhelper.core.SettingsLocationTracker
import kr.petworld.petworld.db.AppDatabase
import kr.petworld.petworld.model.Locations
import kr.petworld.petworld.model.Walks

class LocationReceiver : BroadcastReceiver() {


    //백그라운드 구동할때 위치 저장하기
    override fun onReceive(context: Context, intent: Intent?) {
        if (null != intent && intent.action == "my.action") {
            val locationData =
                intent.getParcelableExtra<Parcelable>(SettingsLocationTracker.LOCATION_MESSAGE) as Location

            val walks = AppDatabase.getInstance(context).databaseDao.lastWalks
            Log.d(
                "Locations: ",
                "Latitude: " + locationData.latitude + "Longitude:" + locationData.longitude
            )

            val locations = Locations()
            locations.latitude = locationData.latitude
            locations.longitude = locationData.longitude
            locations.walkId = walks.id
            AppDatabase.getInstance(context).databaseDao.addLocations(locations)


        }
    }
}
