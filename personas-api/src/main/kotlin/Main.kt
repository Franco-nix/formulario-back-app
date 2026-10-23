package com.ejemplo

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

@Serializable
data class Persona (
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val direccion: String,
    val profesion: String,
    val celular: String,
    val correo: String,
    val fotoRuta: String? = null
)
private val URL = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/personasdb"
private val USER = System.getenv("DB_USER") ?: "postgres"
private val PASS = System.getenv("DB_PASS") ?: "password"

private fun <T> db(block: (Connection) -> T): T =
    DriverManager.getConnection(URL, USER, PASS).use(block)

private fun ResultSet.toPersona() = Persona(
    dni = getString("dni"),
    nombres = getString("nombres"),
    apellidos = getString("apellidos"),
    direccion = getString("direccion") ?: "",
    profesion = getString("profesion")  ?: "",
    celular = getString("celular") ?: "",
    correo = getString("correo") ?: "",
    fotoRuta = getString("foto_ruta")
)

private fun PreparedStatement.bind(p: Persona) {
    setString(1, p.nombres);
    setString(2, p.apellidos);
    setString(3, p.direccion);
    setString(4, p.profesion);
    setString(5, p.celular);
    setString(6, p.correo);
    setString(7, p.fotoRuta);
    setString(8, p.dni)
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys  = true; encodeDefaults = true })
        }
        routing {
            route ("/personas") {
                get("/{dni}") {
                    val dni = call.parameters ["dni"]!!
                    val p = db { c ->
                        c.prepareStatement("SELECT * FROM persona WHERE dni =?").use { ps ->
                            ps.setString(1, dni)
                            ps.executeQuery().use { rs -> if (rs.next()) rs.toPersona() else null }
                        }
                    }
                    if (p == null) call.respond(HttpStatusCode.NotFound) else call.respond(p)
                }
                post {
                    val p = call.receive<Persona>()
                    try {
                        db { c ->
                            c.prepareStatement (
                                "INSERT INTO persona(nombres,apellidos,direccion,profesion,celular,correo,foto_ruta,dni) " + "VALUES (?,?,?,?,?,?,?,?)"
                            ).use { it.bind(p); it.executeUpdate() }
                        }
                        call.respond(HttpStatusCode.Created, p)
                    } catch (e: SQLException) {
                         if (e.sqlState == "23505") call.respond(HttpStatusCode.Conflict) else throw e
                    }
                }
                put("/{dni}") {
                    val p = call.receive<Persona>().copy(dni = call.parameters["dni"]!!)
                    val filas = db { c->
                        c.prepareStatement(
                            "UPDATE persona SET nombres=?,apellidos=?,direccion=?,profesion=?," + "celular=?,correo=?,foto_ruta=? WHERE dni=?"
                        ).use { it.bind(p); it.executeUpdate() }
                    }
                    if (filas == 0) call.respond(HttpStatusCode.NotFound) else call.respond(p)
                }
            }
        }
    }.start(wait = true)
}