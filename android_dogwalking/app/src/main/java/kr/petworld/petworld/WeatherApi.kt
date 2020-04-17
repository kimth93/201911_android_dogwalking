package kr.petworld.petworld

import io.reactivex.Single
import kr.petworld.petworld.model.Forecast
import retrofit2.http.POST
import retrofit2.http.QueryMap
import kotlin.collections.HashMap

interface WeatherApi {


    // 날씨 api
    @POST("weather")
    fun getCurrentWeather(@QueryMap params: HashMap<String, Any>):Single<Forecast>
}