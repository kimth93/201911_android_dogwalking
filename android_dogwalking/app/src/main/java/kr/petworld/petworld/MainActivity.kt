package kr.petworld.petworld

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_content.*
import kr.petworld.petworld.databinding.ActivityContentBinding
import kr.petworld.petworld.model.Main
import kr.petworld.petworld.model.Weather
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.ceil

// 메인 페이지
class MainActivity : AppCompatActivity(), LocationListener {

    override fun onLocationChanged(p0: Location?) {
        this.location = p0
        getWeather()
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    lateinit var activityContentBinding: ActivityContentBinding

    var main: Main? = null


    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    lateinit var waetherApi: WeatherApi
    var mainImagePath: String? = null

    override fun onStart() {
        super.onStart()

        //반려견 정보 확인해서 보여주고 없으면 등록페이지로 이동
        var registered = PreferencesManager.getInstance(this).getValue(
            String::class.java,
            PreferencesManager.Key.registered,
            "no"
        )
        if (registered == "no") {
            var intent = Intent(this, EditInfomationActivity::class.java)
            intent.putExtra("edit", true)
            startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContentBinding = DataBindingUtil.setContentView(this, R.layout.activity_content)
        activityContentBinding.executePendingBindings()
        waetherApi = ApiService.provideApi(WeatherApi::class.java, this)
        supportActionBar!!.hide()

        //위치 받기
        getLocation()


        // 산책하기 선택
        activityContentBinding.startWalking.setOnClickListener {
            var intent = Intent(this, MapsActivity::class.java)
            if (main != null) {
                intent.putExtra("weather", (ceil(main!!.temp!! - 273.15).toInt()).toString())
            }
            startActivity(intent)
        }


        // 산책 일기 click
        activityContentBinding.showListButton.setOnClickListener {
            var intent = Intent(this, WalksListActivity::class.java)
            startActivity(intent)
        }

        // 정보 수정 click
        activityContentBinding.editInformation.setOnClickListener {
            var intent = Intent(this, EditInfomationActivity::class.java)
            intent.putExtra("edit", true)
            startActivity(intent)
        }

        //산책 매칭 click
        activityContentBinding.chattingroom.setOnClickListener {
            var intent = Intent(this, RoomActivity::class.java)
            var nameDog = PreferencesManager.getInstance(this).getValue(
                String::class.java,
                PreferencesManager.Key.dogName,
                ""
            ) as String?

            if(nameDog == "") {
                Toast.makeText(this,"반려견 이름을 등록해주세요",Toast.LENGTH_LONG).show()
            }else {
                intent.putExtra("name", nameDog)
                startActivity(intent)
            }
        }


        //getWeather();
    }


    @SuppressLint("SimpleDateFormat")
    override fun onResume() {
        super.onResume()

        // 반려견 정보 보여주기

        //생일
        var birthday = PreferencesManager.getInstance(this).getValue(
            String::class.java,
            PreferencesManager.Key.birthday,
            ""
        ) as String?


        //성별
        var gender = if ((PreferencesManager.getInstance(
                this
            ).getValue(
                String::class.java,
                PreferencesManager.Key.gender,
                ""
            ) as String?) == "male"
        )"남" else "여"

        //나이 계산
        var age=""
        if (birthday != null && birthday.length > 0) {
            var birthDayDate = SimpleDateFormat("dd-MM-yyyy").parse(birthday)
            var todayDate = Date()

            age=(todayDate.year - birthDayDate!!.year).toString() + " 살 " + gender

        }

        //이름
        var nameDog = PreferencesManager.getInstance(this).getValue(
            String::class.java,
            PreferencesManager.Key.dogName,
            ""
        ) as String?

        activityContentBinding.dogName.text = nameDog

        //정보표시
        activityContentBinding.dogDesc.text = PreferencesManager.getInstance(this).getValue(
            String::class.java,
            PreferencesManager.Key.dogDescription,
            ""
        ) as String? +" "+ age;

        //Image path file
        var path = PreferencesManager.getInstance(this).getValue(
            String::class.java,
            PreferencesManager.Key.mainImagePath,
            ""
        ) as String?
        if (path != null && path.isNotEmpty()) {
            Picasso.get().load(File(path)).into(activityContentBinding.mainImage)
        }
    }


    //갤러리에서 이미지 선택하기
    private fun pickImageFromGallery() {
        //이미지
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //요청 결과 처리하기
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //openweather api에서 날씨 가져오기
    fun getWeather() {
        var params = HashMap<String, Any>()
        if (location != null) {
            params.put("lat", location!!.latitude.toString())
            params.put("lon", location!!.longitude.toString())
        } else {
            params.put("lat", "37.566536")
            params.put("lon", "126.977966")
        }
        //openweather api 앱ID
        params.put("APPID", "102b12da55a7e21146479956c202114c")
        compositeDisposable.add(
            waetherApi.getCurrentWeather(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    //show weather data
                    main = it.main
                    activityContentBinding.temperature.text =
                        Html.fromHtml((ceil(it.main!!.temp!! - 273.15).toInt()).toString() + " &#176;C")
                    activityContentBinding.cityName.text = it.name + ", " + it.sys!!.country
                    activityContentBinding.wind.text = it.wind!!.speed.toString() + "m/s"
                    activityContentBinding.humidity.text = it.main!!.humidity.toString() + "%"
                    activityContentBinding.cloud.text = it.clouds!!.all.toString() + "%"
                    //http://openweathermap.org/img/w/04d.png
                    var url = "http://openweathermap.org/img/w/${it.weather!![0].icon}.png"
                    Log.v("url", url)
                    Glide.with(this@MainActivity).load(url)
                        .into(activityContentBinding.weatherIcon)

                }, { throwable ->
                    throwable.printStackTrace()

                })
        )
    }


    private var location: Location? = null

    //Locations
    lateinit var locationManager: LocationManager
    private var mprovider: String? = null
    private var criteria: Criteria? = null


    //위치가져오기
    fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                this@MainActivity,
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //crop image
        if (requestCode == 1000 && resultCode == RESULT_OK && null != data) {
            //activityContentBinding.mainImage.setImageURI(data?.data)
            // 디바이스에 저장된 이미지 자르기
            CropImage.activity(data?.data)
                .start(this)

        }


        //get cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                var resultUri = result.getUri()
                //show cropped image
                activityContentBinding.mainImage.setImageURI(resultUri)
                //show get Image path
                mainImagePath = FileUtils.getFile(this@MainActivity, resultUri)!!.path


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                var error = result.getError();
            }
        }
    }
}