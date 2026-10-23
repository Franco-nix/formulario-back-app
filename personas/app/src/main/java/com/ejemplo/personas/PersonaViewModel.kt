package com.ejemplo.personas

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

val PROFESIONES = listOf(
    "Ingeniero", "Docente", "Médico", "Enfermero/a", "Abogado",
    "Contador", "Técnico", "Estudiante", "Otro"
)

data class FormState(
    val dni: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val direccion: String = "",
    val profesion: String = "",
    val celular: String = "",
    val correo: String = "",
    val fotoRuta: String? = null,     // foto ya guardada en el backend
    val fotoLocal: String? = null,    // foto recién tomada, pendiente de subir
    val existente: Boolean = false,   // true = el registro ya está en BD (guardar = actualizar)
    val mensaje: String? = null
) {
    fun toPersona() = Persona(dni, nombres.trim(), apellidos.trim(), direccion.trim(),
        profesion, celular.trim(), correo.trim(), fotoRuta)

    /** Lo que debe mostrarse en el círculo de la foto (archivo local o URL del backend). */
    val foto: Any? get() = fotoLocal?.let(::File) ?: fotoRuta?.let(ApiClient::urlFoto)
}

private fun Persona.toForm() = FormState(
    dni = dni, nombres = nombres, apellidos = apellidos, direccion = direccion,
    profesion = profesion, celular = celular, correo = correo,
    fotoRuta = fotoRuta, existente = true
)

class PersonaViewModel : ViewModel() {
    var state by mutableStateOf(FormState())
        private set

    /** Personas guardadas en el backend; null = cargando. */
    var lista by mutableStateOf<List<Persona>?>(emptyList())
        private set
    var listaError by mutableStateOf<String?>(null)
        private set

    private var fotoPendiente: String? = null
    private val api = ApiClient.api

    fun editar(cambio: (FormState) -> FormState) { state = cambio(state) }

    fun limpiarMensaje() { state = state.copy(mensaje = null) }

    /** Opción "Nuevo": limpia todo el formulario. */
    fun nuevo() {
        state.fotoLocal?.let { File(it).delete() }
        state = FormState()
    }

    // ---- Foto ----
    fun prepararFoto(ruta: String) { fotoPendiente = ruta }

    fun onFotoResultado(ok: Boolean) {
        val ruta = fotoPendiente ?: return
        if (ok) {
            state.fotoLocal?.let { File(it).delete() }
            state = state.copy(fotoLocal = ruta)
        } else File(ruta).delete()
        fotoPendiente = null
    }

    // ---- Lista ----
    fun cargarLista() {
        lista = null
        listaError = null
        viewModelScope.launch {
            try {
                val r = api.listar()
                if (r.isSuccessful) lista = r.body().orEmpty()
                else listaError = "Error del servidor (${r.code()})"
            } catch (e: Exception) {
                Log.e("PERSONA", "Fallo de red", e)
                listaError = "No hay conexión con el servidor"
            }
            if (listaError != null) lista = emptyList()
        }
    }

    /** Carga en el formulario una persona elegida de la lista. */
    fun seleccionar(p: Persona) {
        state.fotoLocal?.let { File(it).delete() }
        state = p.toForm()
    }

    // ---- Guardar / Actualizar ----
    fun guardar() {
        val s = state
        val error = when {
            s.dni.length != 8 || !s.dni.all(Char::isDigit) -> "DNI: 8 dígitos"
            s.nombres.isBlank() -> "Falta nombres"
            s.apellidos.isBlank() -> "Falta apellidos"
            s.profesion.isEmpty() -> "Selecciona una profesión"
            s.celular.length != 9 -> "Celular: 9 dígitos"
            !Patterns.EMAIL_ADDRESS.matcher(s.correo).matches() -> "Correo inválido"
            else -> null
        }
        if (error != null) return msg(error)

        viewModelScope.launch {
            try {
                val p = s.toPersona()
                val r = if (s.existente) api.actualizar(p.dni, p) else api.crear(p)
                when {
                    r.isSuccessful -> {
                        state = state.copy(existente = true)
                        val ok = if (s.existente) "Datos actualizados" else "Persona guardada"
                        msg(if (subirFotoPendiente(p.dni)) ok else "$ok, pero no se pudo subir la foto")
                    }
                    r.code() == 409 -> msg("Ese DNI ya existe. Ábrelo desde Ver lista para editarlo")
                    r.code() == 400 -> msg("Datos inválidos según el servidor")
                    else -> msg("Error del servidor (${r.code()})")
                }
            } catch (e: Exception) {
                Log.e("PERSONA", "Fallo de red", e)
                msg("No hay conexión con el servidor")
            }
        }
    }

    /** Sube la foto local (si hay) y la reemplaza por la ruta del backend. */
    private suspend fun subirFotoPendiente(dni: String): Boolean {
        val local = state.fotoLocal ?: return true
        return try {
            val r = api.subirFoto(dni, ApiClient.parteFoto(File(local)))
            if (r.isSuccessful) {
                File(local).delete()
                state = state.copy(fotoRuta = r.body()?.fotoRuta, fotoLocal = null)
            }
            r.isSuccessful
        } catch (e: Exception) {
            Log.e("PERSONA", "Fallo al subir foto", e)
            false
        }
    }

    private fun msg(texto: String) { state = state.copy(mensaje = texto) }
}