package kr.petworld.petworld.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

@Entity
class Walks : Serializable {


    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    var duration: Int = 0

    @ColumnInfo
    var wDate: String? = null

    @ColumnInfo
    var weather: String? = null

    @ColumnInfo
    var distance: Double? = null

    @ColumnInfo
    var memo: String? = null

    @ColumnInfo
    var finished = 0

    fun getwDate(): String {
        return wDate!!
    }

    fun setwDate(wDate: String) {
        this.wDate = wDate
    }
}
