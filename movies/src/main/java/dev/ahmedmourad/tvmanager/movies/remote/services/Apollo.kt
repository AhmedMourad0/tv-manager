package dev.ahmedmourad.tvmanager.movies.remote.services

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.apollographql.apollo.api.CustomTypeValue.GraphQLString
import dev.ahmedmourad.tvmanager.movies.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import type.CustomType
import java.text.ParseException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private object DateCustomTypeAdapter : CustomTypeAdapter<LocalDateTime> {
    private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    override fun decode(value: CustomTypeValue<*>): LocalDateTime {
        return try {
            LocalDateTime.parse(value.value.toString(), format)
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    override fun encode(value: LocalDateTime): CustomTypeValue<*> {
        return GraphQLString(value.format(format))
    }
}

private class AuthorizationInterceptor(
    private val context: Context
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-Parse-Client-Key", context.getString(R.string.x_parse_client_key))
            .addHeader("X-Parse-Application-Id", context.getString(R.string.x_parse_application_id))
            .build()
        return chain.proceed(request)
    }
}

private const val BASE_URL = "https://tv-show-manager.combyne.com/graphql"
internal fun createApolloClient(context: Context): ApolloClient {
    return ApolloClient.builder()
        .serverUrl(BASE_URL)
        .okHttpClient(
            OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(context))
            .build()
        ).addCustomTypeAdapter(CustomType.DATE, DateCustomTypeAdapter)
        .build()
}
