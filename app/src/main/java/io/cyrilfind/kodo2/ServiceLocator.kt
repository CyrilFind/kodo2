package io.cyrilfind.kodo2

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object ServiceLocator {
    // constantes qui serviront à faire les requêtes
    private const val BASE_URL = "https://android-tasks-api.herokuapp.com/api/"

    // client HTTP
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // intercepteur qui ajoute le `header` d'authentification avec votre token:
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${getToken(appContext)}")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    // sérializeur JSON: transforme le JSON en objets kotlin et inversement
    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // instance de convertisseur qui parse le JSON renvoyé par le serveur:
    private val converterFactory =
        jsonSerializer.asConverterFactory("application/json".toMediaType())

    // permettra d'implémenter les services que nous allons créer:
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .build()

    val userWebService: UserWebService by lazy { retrofit.create(UserWebService::class.java) }
    val tasksWebService: TasksWebService by lazy { retrofit.create(TasksWebService::class.java) }
    
    val tasksRepository = TasksRepository()
    
    lateinit var appContext: Context

    fun setUpContext(context: Context) {
        appContext = context
    }
    
    private fun getToken(context: Context): String {
        return "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo1NjMsImV4cCI6MTY3MDg4NzI3Mn0.FEEWybdW6X3h4snVIRfI5X323L1cdveoyE_nWI2qCCc"
    }
}