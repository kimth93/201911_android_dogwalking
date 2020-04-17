package kr.petworld.petworld

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import br.com.safety.locationlistenerhelper.core.LocationTracker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kr.petworld.petworld.databinding.ActivityMapsBinding
import kr.petworld.petworld.db.AppDatabase
import kr.petworld.petworld.model.Locations
import kr.petworld.petworld.model.Walks
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


//산책하기 페이지
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    //google map
    private lateinit var mMap: GoogleMap
    //background location tracker
    private var locationTracker: LocationTracker? = null
    //location
    private var location: Location? = null


    //region Timer
    private var messagelistTimer: Timer? = null
    internal var secondsLeft = 0
    //endregion

    //Locations
    lateinit var locationManager: LocationManager
    private var mprovider: String? = null
    private var criteria: Criteria? = null
    var polyline: Polyline? = null
    lateinit var activityMapsBinding: ActivityMapsBinding
    val points = ArrayList<LatLng>()
    //marker on map(My current location)
    var marker: Marker? = null
    var distance = 0.0


    //산책하는 도중 위치 변경시 위치 저장
    override fun onLocationChanged(p0: Location?) {

        this.location = p0

        var myLoc = LatLng(location!!.latitude, location!!.longitude)
        if (marker == null) {
            //내 위치 표시

            //Image path file
            var path = PreferencesManager.getInstance(this).getValue(
                String::class.java,
                PreferencesManager.Key.mainImagePath,
                ""
            ) as String?

            var markerOptions = MarkerOptions().position(myLoc).title("My location").icon(
                BitmapDescriptorFactory.fromBitmap(
                    resizeBitmap(
                        BitmapFactory.decodeFile(path),
                        100,
                        100
                    )
                )
            )
            marker = mMap.addMarker(
                markerOptions
            )
        } else {
            animateMarker(marker!!, myLoc, false)
        }

        //make zoom to my location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc))
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location!!.latitude,
                    location!!.longitude
                ), 15.0f
            )
        )

        //산책 시작 시 location 저장
        val walks = AppDatabase.getInstance(applicationContext).databaseDao.lastWalks
        if (walks != null && !activityMapsBinding.startWalking.isEnabled) {
            Log.d(
                "Locations: ",
                "Latitude: " + p0!!.latitude + "Longitude:" + p0.longitude
            )
            val locations = Locations()
            locations.latitude = p0!!.latitude
            locations.longitude = p0!!.longitude
            locations.walkId = walks.id
            AppDatabase.getInstance(applicationContext).databaseDao.addLocations(locations)


            //이동경로 그려주기
            points.add(LatLng(locations.latitude, locations.longitude))
            var lineOptions = PolylineOptions()
            lineOptions.addAll(points)
            lineOptions.width(10f)
            lineOptions.color(Color.RED)
            if (polyline != null) {
                polyline!!.remove()
            }
            polyline = mMap.addPolyline(lineOptions)

            if (points.size > 1) {
                distance += calculationByDistance(points[points.size - 2], points[points.size - 1])
                var newFormat = DecimalFormat("####.###")
                activityMapsBinding.walkingDistance.text = newFormat.format(distance) + "km"

            }


        }
    }

    // Method to resize a bitmap programmatically
    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        /*
            *** reference source developer.android.com ***
            Bitmap createScaledBitmap (Bitmap src, int dstWidth, int dstHeight, boolean filter)
                Creates a new bitmap, scaled from an existing bitmap, when possible. If the specified
                width and height are the same as the current width and height of the source bitmap,
                the source bitmap is returned and no new bitmap is created.

            Parameters
                src Bitmap : The source bitmap.
                    This value must never be null.

            dstWidth int : The new bitmap's desired width.
            dstHeight int : The new bitmap's desired height.
            filter boolean : true if the source should be filtered.

            Returns
                Bitmap : The new scaled bitmap or the source bitmap if no scaling is required.

            Throws
                IllegalArgumentException : if width is <= 0, or height is <= 0
        */
        return Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        )
    }

    fun calculationByDistance(StartP: LatLng, EndP: LatLng): Double {
        var Radius = 6371;//radius of earth in Km
        var lat1 = StartP.latitude;
        var lat2 = EndP.latitude;
        var lon1 = StartP.longitude;
        var lon2 = EndP.longitude;
        var dLat = Math.toRadians(lat2 - lat1);
        var dLon = Math.toRadians(lon2 - lon1);
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        var c = 2 * Math.asin(Math.sqrt(a));
        var valueResult = Radius * c;
        var km = valueResult / 1;
        var newFormat = DecimalFormat("####");
        var kmInDec = Integer.valueOf(newFormat.format(km));
        var meter = valueResult % 1000;
        var meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec + " Meter   " + meterInDec);


        return Radius * c;
    }

    //svg에서 내 위치 가져오기
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    //animate marker
    private fun animateMarker(marker: Marker, toPosition: LatLng, hideMarker: Boolean) {
        var handler = Handler();
        var start = SystemClock.uptimeMillis()
        var proj = mMap.projection
        var startPoint = proj.toScreenLocation(marker.position)
        var startLatLng = proj.fromScreenLocation(startPoint)
        var duration = 10000

        var interpolator = LinearInterpolator()

        handler.post(
            SampleRunnable(
                handler,
                marker,
                interpolator,
                start,
                toPosition,
                duration,
                startLatLng,
                hideMarker
            )
        );
    }

    class SampleRunnable(
        var handler: Handler,
        var marker: Marker,
        var interpolator: LinearInterpolator,
        var start: Long,
        var toPosition: LatLng,
        var duration: Int,
        var startLatLng: LatLng,
        var hideMarker: Boolean
    ) : Runnable {
        override fun run() {
            var elapsed = SystemClock.uptimeMillis() - start;
            var t = interpolator.getInterpolation((elapsed / duration).toFloat())
            var lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
            var lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude
            marker.position = LatLng(lat, lng)
            if (t < 1.0) {
                // 16분 후에 다시 게시
                handler.postDelayed(
                    SampleRunnable(
                        handler,
                        marker,
                        interpolator,
                        start,
                        toPosition,
                        duration,
                        startLatLng,
                        hideMarker
                    ), 1000
                );
            } else {
                marker.isVisible = !hideMarker
            }
        }

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }


    //Location setting 가져오기
    @SuppressLint("MissingPermission")
    fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(
            LocationManager.PASSIVE_PROVIDER,
            1000,
            10f,
            this
        )

        criteria = Criteria()
        mprovider = locationManager.getBestProvider(criteria, true)
        if (mprovider != null && mprovider != "") {
            val location = locationManager.getLastKnownLocation(mprovider)
            //send request, 10 second 10 m 산책할대 location change is working
            locationManager.requestLocationUpdates(mprovider, 15000, 15f, this)
            if (location != null)
                onLocationChanged(location)

        }
    }


    // activity start
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMapsBinding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        activityMapsBinding.executePendingBindings()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Location permission 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MapsActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000
            )
            return
        }
        //산책시작
        activityMapsBinding.startWalking.setOnClickListener {
            Toast.makeText(this, "Walking is starting...", Toast.LENGTH_LONG).show()
            var walk = Walks()
            //산책 db에 저장
            AppDatabase.getInstance(this).databaseDao.addWalks(walk)
            //timer start
            restartMessageListTimer(1000)
            //시작버튼 안눌렀을때
            activityMapsBinding.startWalking.isEnabled = false
            //산책종료 활성화
            activityMapsBinding.endWalking.isEnabled = true
            onLocationChanged(location)
        }
        //산책종료
        activityMapsBinding.endWalking.setOnClickListener {

            var lastWalk = AppDatabase.getInstance(this).databaseDao.lastWalks
            lastWalk.duration = secondsLeft
            lastWalk.weather = intent.getStringExtra("weather")
            lastWalk.distance = distance
            lastWalk.finished = 1
            //update walk  again

            AppDatabase.getInstance(this).databaseDao.addWalks(lastWalk)
            //timer  stop
            if (messagelistTimer != null) {
                messagelistTimer!!.cancel()
                messagelistTimer!!.purge()
            }
            //산책 시작 버튼
            activityMapsBinding.startWalking.isEnabled = true
            //산책 종료 버튼
            activityMapsBinding.endWalking.isEnabled = false
            //산책종료 페이지로 이동
            var intent = Intent(this, FinishWalkingActivity::class.java)
            intent.putExtra("walk", lastWalk)
            startActivity(intent)
            finish()
        }


    }

    var walkingStarted = false

    override fun onBackPressed() {

        if (!walkingStarted) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Please finish walking to leave page", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * @param delay Start timer count down after delay milliseconds
     */
    private fun restartMessageListTimer(delay: Long) {
        if (messagelistTimer != null) {
            messagelistTimer!!.cancel()
            messagelistTimer!!.purge()
        }
        activityMapsBinding.walkingTimeTextView.visibility = View.VISIBLE
        messagelistTimer = Timer()
        messagelistTimer!!.schedule(SecondTimerTask(), delay, 1000)
    }

    /**
     * Timer task function
     */
    private inner class SecondTimerTask : TimerTask() {
        @SuppressLint("SetTextI18n", "MissingPermission")
        override fun run() {
            secondsLeft++
            runOnUiThread {
                val hour = secondsLeft / 3600

                val min = (secondsLeft - hour * 3600) / 60
                val sec = secondsLeft - hour * 3600 - min * 60
                activityMapsBinding.walkingTimeTextView.text =
                    "" + if (hour > 9) hour else "0" + hour + ":" + if (min > 9) min else "0" + min + ":" + if (sec > 9) sec else "0$sec"


            }

        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        getLocation()

    }

    override fun onStart() {
        super.onStart()
        locationTracker = LocationTracker("my.action")

            .setInterval(1000)
            .setGps(true)
            .setNetWork(false)


        // .start(baseContext, this)

        // IF YOU WANT RUN IN SERVICE
        locationTracker?.start(baseContext, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        locationTracker!!.onRequestPermission(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onDestroy() {
        super.onDestroy()
        locationTracker!!.stopLocationService(this)
    }
}
