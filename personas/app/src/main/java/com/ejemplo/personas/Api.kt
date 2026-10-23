package com.ejemplo.personas

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.*

@Serializable
data class Persona(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val direccion: String,
    val profesion: String,
    val celular: String,
    val correo: String,
    val fotoRuta: String? = null
)

interface PersonaApi {
    @GET("personas/{dni}")
    suspend fun buscar(@Path("dni") dni: String): Response<Persona>

    @POST("personas")
    suspend fun crear(@Body p: Persona): Response<Persona>

    @PUT("personas/{dni}")
    suspend fun actualizar(@Path("dni") dni: String, @Body p: Persona): Response<Persona>
}

object ApiClient {
    // Emulador: 10.0.2.2 apunta al localhost de tu PC.
    // Celular físico: usa la IP LAN de tu PC, ej. "http://192.168.1.50:8080/"
    //"http://10.0.2.2:8080/"
    private const val BASE_URL = "//http://10.0.2.2:8080/"

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val api: PersonaApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PersonaApi::class.java)
}