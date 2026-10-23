package com.ejemplo.personas

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.*
import java.io.File

@Serializable
data class Persona(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val direccion: String = "",
    val profesion: String = "",
    val celular: String = "",
    val correo: String = "",
    val fotoRuta: String? = null   // ruta en el servidor, ej. "uploads/12345678-1700000000000.jpg"
)

interface PersonaApi {
    @GET("personas")
    suspend fun listar(): Response<List<Persona>>

    @GET("personas/{dni}")
    suspend fun buscar(@Path("dni") dni: String): Response<Persona>

    @POST("personas")
    suspend fun crear(@Body p: Persona): Response<Persona>

    @PUT("personas/{dni}")
    suspend fun actualizar(@Path("dni") dni: String, @Body p: Persona): Response<Persona>

    @DELETE("personas/{dni}")
    suspend fun eliminar(@Path("dni") dni: String): Response<Unit>

    @Multipart
    @POST("personas/{dni}/foto")
    suspend fun subirFoto(@Path("dni") dni: String, @Part foto: MultipartBody.Part): Response<Persona>
}

object ApiClient {
    // Emulador: 10.0.2.2 apunta al localhost de tu PC.
    // Celular físico: usa la IP LAN de tu PC, ej. "http://192.168.1.50:8080/"
    const val BASE_URL = "http://192.168.100.180:8080/"

    // coerceInputValues: si el backend manda null en un campo opcional, se usa "" por defecto
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; coerceInputValues = true }

    /** URL pública de una foto guardada en el backend. */
    fun urlFoto(fotoRuta: String) = BASE_URL + fotoRuta

    fun parteFoto(archivo: File): MultipartBody.Part = MultipartBody.Part.createFormData(
        "foto", archivo.name, archivo.asRequestBody("image/jpeg".toMediaType())
    )

    val api: PersonaApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PersonaApi::class.java)
}