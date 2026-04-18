package xyz.doocode.superbus.core.api

import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.doocode.superbus.BuildConfig

/** Factory and holder for API clients. */
object ApiClient {

    private const val GINKO_BASE_URL = "https://api.ginko.voyage/"
    private const val JCDECAUX_BASE_URL = "https://api.jcdecaux.com/"

    // Timeout configuration
    private const val TIMEOUT_SECONDS = 30L

    /** Lazy-loaded GinkoApiService instance. */
    val ginkoService: GinkoApiService by lazy {
        val client = createOkHttpClient(apiKeyParamName = null, apiKeyValue = null)

        buildRetrofit(GINKO_BASE_URL, client).create(GinkoApiService::class.java)
    }

    /** Lazy-loaded JCDecauxApiService instance (with ISO 8601 date support). */
    val jcDecauxService: JCDecauxApiService by lazy {
        val client =
            createOkHttpClient(
                apiKeyParamName = "apiKey",
                apiKeyValue = BuildConfig.VELOCITE_API_KEY
            )
        val gson =
            GsonBuilder()
                .registerTypeAdapter(Long::class.java, DateDeserializer)
                .registerTypeAdapter(Long::class.javaObjectType, DateDeserializer)
                .create()

        buildRetrofit(JCDECAUX_BASE_URL, client, gson).create(JCDecauxApiService::class.java)
    }

    /**
     * Creates a standardized OkHttpClient with logging and, when needed, a query-parameter
     * interceptor for API keys.
     *
     * @param apiKeyParamName The query parameter name for the API key when an API expects it in
     *   the URL.
     * @param apiKeyValue The actual API key value.
     */
    private fun createOkHttpClient(apiKeyParamName: String?, apiKeyValue: String?): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

        val builder =
            OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (apiKeyParamName != null && apiKeyValue != null) {
            builder.addInterceptor(
                Interceptor { chain ->
                    val original = chain.request()
                    val originalHttpUrl = original.url

                    // Add the API key to every request as a query parameter when required.
                    val url =
                        originalHttpUrl
                            .newBuilder()
                            .addQueryParameter(apiKeyParamName, apiKeyValue)
                            .build()

                    val requestBuilder = original.newBuilder().url(url)

                    chain.proceed(requestBuilder.build())
                }
            )
        }

        return builder.build()
    }

    /** Builds a Retrofit instance for a specific base URL, client and optional Gson instance. */
    private fun buildRetrofit(
        baseUrl: String,
        client: OkHttpClient,
        gson: com.google.gson.Gson = com.google.gson.Gson()
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
