package xyz.doocode.superbus.core.api

import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.dto.EtatLigne
import xyz.doocode.superbus.core.dto.Ligne
import xyz.doocode.superbus.core.dto.Message
import xyz.doocode.superbus.core.dto.TempsLieu
import xyz.doocode.superbus.core.dto.VehiculeDR
import xyz.doocode.superbus.core.dto.VehiculeTR
import xyz.doocode.superbus.core.dto.Affluence
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface defining the endpoints for the Ginko Mobility API.
 */
interface GinkoApiService {

    // --- DR: Reference Data ---

    @GET("DR/getArrets.do")
    suspend fun getArrets(): GinkoResponse<List<Arret>>

    @GET("DR/getLignes.do")
    suspend fun getLignes(): GinkoResponse<List<Ligne>>

    @GET("DR/getDetailsVariante.do")
    suspend fun getDetailsVariante(
        @Query("idLigne") idLigne: String,
        @Query("idVariante") idVariante: String
    ): GinkoResponse<List<Arret>>

    @GET("DR/getVariantesDesservantArret.do")
    suspend fun getVariantesDesservantArret(
        @Query("idArret") idArret: String
    ): GinkoResponse<List<Ligne>>

    @GET("DR/getArretsProches.do")
    suspend fun getArretsProches(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): GinkoResponse<List<Arret>>

    @GET("DR/getDetailsArret.do")
    suspend fun getDetailsArret(
        @Query("id") id: String
    ): GinkoResponse<Arret>

    @GET("DR/getDetailsVehicule.do")
    suspend fun getDetailsVehiculeDR(
        @Query("num") num: Int
    ): GinkoResponse<VehiculeDR>



    // --- TR: Real Time ---

    @GET("TR/getTempsLieu.do")
    suspend fun getTempsLieu(
        @Query("nom") nom: String? = null,
        @Query("idArret") idArret: String? = null,
        @Query("nb") nb: Int? = null
    ): GinkoResponse<TempsLieu>

    @GET("TR/getListeTemps.do")
    suspend fun getListeTemps(
        @Query("listeNoms") listeNoms: List<String>,
        @Query("listeIdLignes") listeIdLignes: List<String>? = null,
        @Query("listeSensAller") listeSensAller: List<Boolean>,
        @Query("preserverOrdre") preserverOrdre: Boolean? = null,
        @Query("nb") nb: Int? = null
    ): GinkoResponse<List<TempsLieu>>

    @GET("TR/getEtatLignes.do")
    suspend fun getEtatLignes(): GinkoResponse<List<EtatLigne>>

    @GET("TR/getMessages.do")
    suspend fun getMessages(
        @Query("idLignes") idLignes: List<String>? = null,
        @Query("idVariantes") idVariantes: List<String>? = null
    ): GinkoResponse<List<Message>>

    @GET("TR/getDetailsVehicule.do")
    suspend fun getDetailsVehiculeTR(
        @Query("num") num: Int
    ): GinkoResponse<VehiculeTR>



    // --- Affluence ---

    @GET("Affluence/get.do")
    suspend fun getAffluence(
        @Query("idArret") idArret: String,
        @Query("date") date: String, // Format YYYY-MM-DD
        @Query("idLigne") idLigne: String,
        @Query("sensAller") sensAller: Boolean
    ): GinkoResponse<Affluence>
}
