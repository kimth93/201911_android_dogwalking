package kr.petworld.petworld.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

@Entity
class Locations : Serializable {


    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo
    var walkId: Int = 0

    @ColumnInfo
    var latitude: Double = 0.toDouble()


    @ColumnInfo
    var longitude: Double = 0.toDouble()
}
