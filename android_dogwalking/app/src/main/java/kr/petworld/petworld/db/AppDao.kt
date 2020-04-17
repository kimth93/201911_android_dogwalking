package kr.petworld.petworld.db


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kr.petworld.petworld.model.Locations
import kr.petworld.petworld.model.Walks


@Dao
interface AppDao {

    @get:Query("SELECT * FROM Walks order by id desc")
    val waksList: List<Walks>

    @get:Query("SELECT * FROM Walks where finished==0 limit 1")
    val lastWalks: Walks

    @get:Query("SELECT * FROM Walks")
    val walks: List<Walks>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWalks(walks: Walks)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocations(locations: Locations)

    @Query("SELECT * FROM Locations where walkId=:walkId")
    fun getLocations(walkId: Int): List<Locations>

    @Delete
    fun deleteWalks(walks: Walks)


}
