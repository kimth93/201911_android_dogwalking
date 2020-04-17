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
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import kr.petworld.petworld.databinding.ActivityFinishWalkingBinding
import kr.petworld.petworld.db.AppDatabase
import kr.petworld.petworld.model.Walks
import java.text.DecimalFormat


class DetailsWalkingActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

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


    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    //저장했던 지도를 다시 출력
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocation()
        //getLocation()
        var walkTrackingLocations = AppDatabase.getInstance(this).databaseDao.getLocations(walk.id)
        val points = ArrayList<LatLng>()
        for (locations in walkTrackingLocations) {
            points.add(LatLng(locations.latitude, locations.longitude))
        }

        var lineOptions = PolylineOptions()
        lineOptions.addAll(points)
        lineOptions.width(10f)
        lineOptions.color(Color.RED)

        // 구글 지도에서 i-th route 이동경로 그리기
        mMap.addPolyline(lineOptions)


        if (points.size > 0) {
            //zoom으로 시작 위치 표시
            mMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)))
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    points.get(0), 15.0f
                )
            )
        }


    }

    //저장된 위치로 이동하도록 지정
    fun animateMarker(marker: Marker, toPosition: LatLng, hideMarker: Boolean) {
        var handler = Handler();
        var start = SystemClock.uptimeMillis();
        var proj = mMap.getProjection()
        var startPoint = proj.toScreenLocation(marker.getPosition());
        var startLatLng = proj.fromScreenLocation(startPoint);
        var duration = 500;

        var interpolator = LinearInterpolator();

        handler.post(Runnable() {
            @Override
            fun run() {
                var elapsed = SystemClock.uptimeMillis() - start;
                var t = interpolator.getInterpolation((elapsed / duration).toFloat());
                var lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                var lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.position = LatLng(lat, lng)
            }
        });
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
                this@DetailsWalkingActivity,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFinishWalkingBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_finish_walking)
        activityFinishWalkingBinding!!.executePendingBindings()
        walk = intent.getSerializableExtra("walk") as Walks

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        activityFinishWalkingBinding!!.dateOfWalkingTextView.setText(walk!!.wDate)
        activityFinishWalkingBinding!!.walkingMemoTextview.setText(walk!!.memo)

        //산책 정보 저장
        activityFinishWalkingBinding!!.saveWalking.setOnClickListener {
            var noteMemo = activityFinishWalkingBinding!!.walkingMemoTextview.text.toString()
            walk.memo = noteMemo
            walk.wDate = activityFinishWalkingBinding!!.dateOfWalkingTextView.text.toString()
            AppDatabase.getInstance(this).databaseDao.addWalks(walk)
            finish()
        }


        var newFormat = DecimalFormat("####.###")
        if (walk.distance != null) {
            activityFinishWalkingBinding!!.walkingDistance.text = newFormat.format(walk.distance!!)+" km"
        }

    }


}