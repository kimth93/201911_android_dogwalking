package kr.petworld.petworld

import android.content.Context

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Bek on 05/07/2017.
 */

object ApiService {
    private var mRetrofit: Retrofit? = null

    var connectionTime = 1

    fun provideRetrofit(context: Context): Retrofit {
        if (mRetrofit == null) {
            val gson = GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create()

            mRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(provideOkHttpClient(context))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return mRetrofit!!
    }

    private fun provideOkHttpClient(context: Context): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor(provideHttpLoggingInterceptor())
            .build()
    }

    private fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        val logLevel =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BODY
        interceptor.setLevel(logLevel)
        return interceptor
    }

    fun <T> provideApi(service: Class<T>, context: Context): T {
        return provideRetrofit(context).create(service)
    }
}

