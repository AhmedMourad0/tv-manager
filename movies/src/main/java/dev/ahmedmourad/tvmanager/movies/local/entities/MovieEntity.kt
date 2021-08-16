package dev.ahmedmourad.tvmanager.movies.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ahmedmourad.tvmanager.movies.local.LocalContract.Movie

@Entity(tableName = Movie.TABLE_NAME)
internal data class MovieEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = Movie.COL_ID)
    val id: String,

    @ColumnInfo(name = Movie.COL_TITLE)
    val title: String,

    @ColumnInfo(name = Movie.COL_CREATED_AT)
    val createdAt: Long,

    @ColumnInfo(name = Movie.COL_RELEASE_DATE)
    val releaseDate: Long?,

    @ColumnInfo(name = Movie.COL_SEASONS_COUNT)
    val seasonsCount: Double?
)
