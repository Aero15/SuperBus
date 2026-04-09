package xyz.doocode.superbus.core.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import xyz.doocode.superbus.core.dto.jcdecaux.Station

interface JCDecauxApiService {
    @GET("vls/v3/stations")
    suspend fun getStations(@Query("contract") contract: String = "besancon"): List<Station>

    @GET("vls/v3/stations/{station_number}")
    suspend fun getStation(
        @Path("station_number") stationNumber: Int,
        @Query("contract") contract: String = "besancon"
    ): Station
}
