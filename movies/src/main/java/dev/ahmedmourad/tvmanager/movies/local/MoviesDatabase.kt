package dev.ahmedmourad.tvmanager.movies.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.ahmedmourad.tvmanager.movies.local.entities.*
import dev.ahmedmourad.tvmanager.movies.local.daos.MoviesDao

@Database(entities = [MovieEntity::class], version = 1)
internal abstract class MoviesDatabase : RoomDatabase() {

    abstract fun usersDao(): MoviesDao

    companion object {

        @Volatile
        private var INSTANCE: MoviesDatabase? = null

        fun getInstance(appCtx: Context) = INSTANCE ?: synchronized(MoviesDatabase::class.java) {
            INSTANCE ?: buildDatabase(appCtx).also { INSTANCE = it }
        }

        private fun buildDatabase(appCtx: Context) = Room.databaseBuilder(
            appCtx,
            MoviesDatabase::class.java,
            LocalContract.DATABASE_NAME
        ).build()
    }
}
