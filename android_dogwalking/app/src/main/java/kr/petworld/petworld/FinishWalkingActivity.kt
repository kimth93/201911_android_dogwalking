package kr.petworld.petworld

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kr.petworld.petworld.databinding.ActivityFinishWalkingBinding
import kr.petworld.petworld.db.AppDatabase
import kr.petworld.petworld.model.Walks
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FinishWalkingActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var walk: Walks
    private lateinit var mMap: GoogleMap

    private var location: Location? = null

    //Locations
    lateinit var locationManager: LocationManager
    private var mprovider: String? = null
    private var criteria: Criteria? = null

    //산책하는 도중 위치 변경시 위치 저장
    override fun onLocationChanged(p0: Location?) {
        this.location = p0


        var myLoc = LatLng(location!!.latitude, location!!.longitude)
        //mMap.addMarker(MarkerOptions().position(myLoc).title("My location"))


        //내 위치 확대
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc))
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location!!.latitude,
                    location!!.longitude
                ), 15.0f
            )
        )
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getLocation()
        var walkTrackingLocations = AppDatabase.getInstance(this).databaseDao.getLocations(walk.id)
        val points = ArrayList<LatLng>()
        for (locations in walkTrackingLocations) {
            points.add(LatLng(locations.latitude, locations.longitude))
        }
        //산책 시작할때 위치 저장하고 이동경로 그리기
        var lineOptions = PolylineOptions()
        lineOptions.addAll(points)
        lineOptions.width(5f)
        lineOptions.color(Color.RED)

        //구글 맵 경로 그리기
        mMap.addPolyline(lineOptions)

    }


    @SuppressLint("MissingPermission")
    fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                1000,
                1f,
                this
            );
        } catch (ex: java.lang.SecurityException) {
            Log.i("test", "fail to request location update, ignore", ex);
        } catch (ex: IllegalArgumentException) {
            Log.d("test", "network provider does not exist, " + ex.message);
        }
        criteria = Criteria()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@FinishWalkingActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000
            )
            return
        }
        mprovider = locationManager.getBestProvider(criteria, true)

        if (mprovider != null && mprovider != "") {

            val location = locationManager.getLastKnownLocation(mprovider)
            locationManager.requestLocationUpdates(mprovider, 15000, 15f, this)


            if (location != null)
                onLocationChanged(location)

        }
    }


    private var activityFinishWalkingBinding: ActivityFinishWalkingBinding? = null

    // activity start
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFinishWalkingBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_finish_walking)
        activityFinishWalkingBinding!!.executePendingBindings()
        walk = intent.getSerializableExtra("walk") as Walks


        activityFinishWalkingBinding!!.dateOfWalkingTextView.setText(
            SimpleDateFormat("dd-MM-yyyy").format(
                Date()
            )
        )

        //지도사용 준비 됐을때 SupportMapFragment와 알림
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        var newFormat = DecimalFormat("####.###")
        activityFinishWalkingBinding!!.walkingDistance.text =
            newFormat.format(walk.distance) + " km"

        //산책 정보 저장
        activityFinishWalkingBinding!!.saveWalking.setOnClickListener {
            var noteMemo = activityFinishWalkingBinding!!.walkingMemoTextview.text.toString()
            walk.memo = noteMemo
            walk.wDate = activityFinishWalkingBinding!!.dateOfWalkingTextView.text.toString()
            AppDatabase.getInstance(this).databaseDao.addWalks(walk)
            finish()
        }

    }


}