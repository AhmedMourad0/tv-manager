package dev.ahmedmourad.tvmanager.movies.local

internal object LocalContract {

    const val DATABASE_NAME = "movies"

    object Movie {
        const val TABLE_NAME = "movie"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_CREATED_AT = "created_at"
        const val COL_RELEASE_DATE = "release_date"
        const val COL_SEASONS_COUNT = "seasons_count"
    }
}
