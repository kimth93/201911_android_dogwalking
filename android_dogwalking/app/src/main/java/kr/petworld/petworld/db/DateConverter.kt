package kr.petworld.petworld.db


import android.annotation.SuppressLint

import androidx.room.TypeConverter

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

object DateConverter {

    @SuppressLint("SimpleDateFormat")
    internal var df: DateFormat = SimpleDateFormat("yyyy-MM-dd")

    @TypeConverter
    fun fromDate(value: String?): Date? {
        if (value != null) {
            try {
                return df.parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return null
        } else {
            return null
        }
    }
}
