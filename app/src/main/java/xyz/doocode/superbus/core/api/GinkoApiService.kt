package xyz.doocode.superbus.core.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import xyz.doocode.superbus.BuildConfig
import xyz.doocode.superbus.core.dto.ginko.Affluence
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.ginko.EtatLigne
import xyz.doocode.superbus.core.dto.ginko.Ligne
import xyz.doocode.superbus.core.dto.ginko.Message
import xyz.doocode.superbus.core.dto.ginko.TempsLieu
import xyz.doocode.superbus.core.dto.ginko.VehiculeDR
import xyz.doocode.superbus.core.dto.ginko.VehiculeTR

/**
 * Retrofit interface defining the endpoints for the Ginko Mobility API.
 */
interface GinkoApiService {

    // --- DR: Reference Data ---

    @FormUrlEncoded
    @POST("DR/getArrets.do")
    suspend fun getArrets(
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<Arret>>

    @FormUrlEncoded
    @POST("DR/getLignes.do")
    suspend fun getLignes(
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<Ligne>>

    @FormUrlEncoded
    @POST("DR/getDetailsVariante.do")
    suspend fun getDetailsVariante(
        @Field("idLigne") idLigne: String,
        @Field("idVariante") idVariante: String,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<Arret>>

    @FormUrlEncoded
    @POST("DR/getVariantesDesservantArret.do")
    suspend fun getVariantesDesservantArret(
        @Field("idArret") idArret: String,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<Ligne>>

    @FormUrlEncoded
    @POST("DR/getArretsProches.do")
    suspend fun getArretsProches(
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<Arret>>

    @FormUrlEncoded
    @POST("DR/getDetailsArret.do")
    suspend fun getDetailsArret(
        @Field("id") id: String,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<Arret>

    @FormUrlEncoded
    @POST("DR/getDetailsVehicule.do")
    suspend fun getDetailsVehiculeDR(
        @Field("num") num: String,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<VehiculeDR>



    // --- TR: Real Time ---

    @FormUrlEncoded
    @POST("TR/getTempsLieu.do")
    suspend fun getTempsLieu(
        @Field("nom") nom: String? = null,
        @Field("idArret") idArret: String? = null,
        @Field("nb") nb: Int? = null,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<TempsLieu>

    @FormUrlEncoded
    @POST("TR/getListeTemps.do")
    suspend fun getListeTemps(
        @Field("listeNoms") listeNoms: List<String>,
        @Field("listeIdLignes") listeIdLignes: List<String>? = null,
        @Field("listeSensAller") listeSensAller: List<Boolean>,
        @Field("preserverOrdre") preserverOrdre: Boolean? = null,
        @Field("nb") nb: Int? = null,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<TempsLieu>>

    @FormUrlEncoded
    @POST("TR/getEtatLignes.do")
    suspend fun getEtatLignes(
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<EtatLigne>>

    @FormUrlEncoded
    @POST("TR/getMessages.do")
    suspend fun getMessages(
        @Field("idLignes") idLignes: List<String>? = null,
        @Field("idVariantes") idVariantes: List<String>? = null,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<List<Message>>

    @FormUrlEncoded
    @POST("TR/getDetailsVehicule.do")
    suspend fun getDetailsVehiculeTR(
        @Field("num") num: Int,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<VehiculeTR>



    // --- Affluence ---

    @FormUrlEncoded
    @POST("Affluence/get.do")
    suspend fun getAffluence(
        @Field("idArret") idArret: String,
        @Field("date") date: String, // Format YYYY-MM-DD
        @Field("idLigne") idLigne: String,
        @Field("sensAller") sensAller: Boolean,
        @Field("apiKey") apiKey: String = BuildConfig.GINKO_API_KEY
    ): GinkoResponse<Affluence>
}
