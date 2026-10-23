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
    val fotoRuta: String? = null,
    val existente: Boolean = false,   // true = el registro ya está en BD (guardar = actualizar)
    val mensaje: String? = null
) {
    fun toPersona() = Persona(dni, nombres.trim(), apellidos.trim(), direccion.trim(),
        profesion, celular.trim(), correo.trim(), fotoRuta)
}

private fun Persona.toForm() = FormState(
    dni = dni, nombres = nombres, apellidos = apellidos, direccion = direccion,
    profesion = profesion, celular = celular, correo = correo,
    fotoRuta = fotoRuta, existente = true
)

class PersonaViewModel : ViewModel() {
    var state by mutableStateOf(FormState())
        private set

    private var fotoPendiente: String? = null
    private val api = ApiClient.api

    fun editar(cambio: (FormState) -> FormState) { state = cambio(state) }

    fun limpiarMensaje() { state = state.copy(mensaje = null) }

    /** Opción "Nuevo": limpia todo el formulario. */
    fun nuevo() { state = FormState() }

    // ---- Foto ----
    fun prepararFoto(ruta: String) { fotoPendiente = ruta }

    fun onFotoResultado(ok: Boolean) {
        val ruta = fotoPendiente ?: return
        if (ok) state = state.copy(fotoRuta = ruta) else File(ruta).delete()
        fotoPendiente = null
    }

    // ---- Buscar ----
    fun buscar() {
        if (state.dni.length != 8) return msg("Ingresa un DNI de 8 dígitos")
        viewModelScope.launch {
            try {
                val r = api.buscar(state.dni)
                when {
                    r.isSuccessful -> state = r.body()!!.toForm().copy(mensaje = "Persona encontrada")
                    r.code() == 404 -> msg("No existe una persona con ese DNI")
                    else -> msg("Error del servidor (${r.code()})")
                }
            } catch (e: Exception) {
                Log.e("PERSONA", "Fallo de red", e)
                msg("Error: ${e::class.simpleName} - ${e.message}")
            }
        }
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
                    r.isSuccessful -> state = state.copy(
                        existente = true,
                        mensaje = if (s.existente) "Datos actualizados" else "Persona guardada"
                    )
                    r.code() == 409 -> msg("Ese DNI ya existe. Usa Buscar para editarlo")
                    else -> msg("Error del servidor (${r.code()})")
                }
            } catch (e: Exception) { msg("No hay conexión con el servidor") }
        }
    }

    private fun msg(texto: String) { state = state.copy(mensaje = texto) }
}