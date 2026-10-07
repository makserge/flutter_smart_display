package com.smsoft.smartdisplay.network

import com.smsoft.smartdisplay.BuildConfig
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

interface WeatherApiService {
    @GET("onecall")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely, hourly",
        @Query("appid") appId: String = BuildConfig.OWM_API_KEY,
    ): WeatherResult.Success
}

class WeatherApi(moshi: Moshi) {
    private val retrofit: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/3.0/")
            // Bounds the whole request, so a hanging connection can't keep the update busy
            .client(
                OkHttpClient.Builder()
                    .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }

    suspend fun getForecast(lat: Double, lon: Double): WeatherResult {
        return try {
            retrofit.getForecast(lat, lon)
        } catch (e: CancellationException) {
            // A cancelled worker must stop, not report a failed request
            throw e
        } catch (e: HttpException) {
            val code = e.code()
            val kind = when {
                (code == 401) || (code == 403) -> WeatherFailureKind.AUTH
                (code == 429) || (code >= 500) -> WeatherFailureKind.SERVER
                else -> WeatherFailureKind.CLIENT
            }
            WeatherResult.Failure(
                error = "HTTP $code",
                kind = kind,
                httpCode = code
            )
        } catch (e: JsonDataException) {
            failure(e, WeatherFailureKind.PARSE)
        } catch (e: JsonEncodingException) {
            // Must stay before IOException, which it extends
            failure(e, WeatherFailureKind.PARSE)
        } catch (e: IOException) {
            failure(e, WeatherFailureKind.NETWORK)
        } catch (e: Exception) {
            failure(e, WeatherFailureKind.UNKNOWN)
        }
    }

    // The request URL carries the API key, so mask it in case a message contains the URL
    private fun failure(e: Exception, kind: WeatherFailureKind) = WeatherResult.Failure(
        error = "${e.javaClass.simpleName}: ${e.message}".replace(API_KEY_PATTERN, "appid=***"),
        kind = kind
    )
}

private const val CALL_TIMEOUT_SECONDS = 30L
private val API_KEY_PATTERN = Regex("appid=[^&\\s]*")
