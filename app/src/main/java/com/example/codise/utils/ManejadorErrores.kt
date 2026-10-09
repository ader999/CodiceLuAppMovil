package com.example.codise.utils

import com.example.codise.data.IdiomaApp
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ManejadorErrores {

    enum class TipoOperacion {
        GENERAL,
        LOGIN,
        REGISTRO
    }

    enum class CategoriaError {
        NO_ENCONTRADO,      // 404
        SERVIDOR,           // 50X (500, 502, 503, 504)
        RED,                // Sin conexión / timeout
        SESION_EXPIRADA,    // 401, 403
        SOLICITUD_INVALIDA, // 400
        GENERAL             // Desconocido / otro
    }

    data class DetallesErrorVisual(
        val categoria: CategoriaError,
        val titulo: String,
        val mensaje: String,
        val sugerencia: String,
        val textoBotonPrincipal: String,
        val textoBotonSecundario: String
    )

    @Volatile
    var idiomaActual: IdiomaApp = IdiomaApp.ESPANOL

    /**
     * Resuelve excepciones (red, timeout, formato, etc.) a un mensaje entendible por el usuario,
     * evitando mostrar excepciones técnicas como "Unable to resolve host", "NullPointerException", etc.
     */
    fun obtenerMensajeError(
        e: Throwable,
        idioma: IdiomaApp = idiomaActual
    ): String {
        val cadenas = CadenasIdiomas.obtener(idioma)

        return when {
            esErrorDeRed(e) -> {
                cadenas.errorConexionRed
            }
            esErrorDeDatos(e) -> {
                cadenas.errorDatos
            }
            else -> {
                val mensaje = e.localizedMessage ?: e.message
                if (mensaje != null && !esMensajeTecnico(mensaje)) {
                    mensaje
                } else {
                    cadenas.errorServidor
                }
            }
        }
    }

    /**
     * Procesa la respuesta HTTP fallida de Retrofit y entrega un mensaje claro y limpio
     * en lugar de cadenas tipo "Error: 400 - Bad Request".
     */
    fun <T> obtenerMensajeErrorHttp(
        respuesta: Response<T>,
        tipoOperacion: TipoOperacion = TipoOperacion.GENERAL,
        idioma: IdiomaApp = idiomaActual
    ): String {
        val codigo = respuesta.code()
        val cuerpoError = try {
            respuesta.errorBody()?.string()
        } catch (_: Exception) {
            null
        }

        return parsearErrorHttp(codigo, cuerpoError, tipoOperacion, idioma)
    }

    /**
     * Parsea un código HTTP y un posible cuerpo JSON de error.
     */
    fun parsearErrorHttp(
        codigo: Int,
        cuerpoError: String?,
        tipoOperacion: TipoOperacion = TipoOperacion.GENERAL,
        idioma: IdiomaApp = idiomaActual
    ): String {
        val cadenas = CadenasIdiomas.obtener(idioma)

        // 1. Caso Login (400 Bad Request o 401 Unauthorized por credenciales inválidas)
        if (tipoOperacion == TipoOperacion.LOGIN && (codigo == 400 || codigo == 401)) {
            val mensajeExtraido = extraerMensajeDeJson(cuerpoError)
            if (mensajeExtraido != null) {
                val traducido = traducirMensajeComun(mensajeExtraido, idioma)
                if (traducido != null) return traducido
            }
            return cadenas.errorCredenciales
        }

        // 2. Caso Registro (400 con validaciones específicas de campos)
        if (tipoOperacion == TipoOperacion.REGISTRO && codigo == 400) {
            val formateado = formatearErroresRegistro(cuerpoError, idioma)
            if (formateado != null) return formateado
            return when (idioma) {
                IdiomaApp.INGLES -> "The registration details are invalid. Please check each field."
                IdiomaApp.CHINO -> "注册信息无效。请检查所有输入内容。"
                else -> "Los datos de registro son inválidos. Por favor revisa cada campo."
            }
        }

        // 3. Códigos HTTP generales
        return when (codigo) {
            400 -> {
                val extraido = extraerMensajeDeJson(cuerpoError)
                if (extraido != null) {
                    val traducido = traducirMensajeComun(extraido, idioma)
                    traducido ?: extraido
                } else {
                    when (idioma) {
                        IdiomaApp.INGLES -> "Invalid request. Please verify the information."
                        IdiomaApp.CHINO -> "请求无效。请核对提交的信息。"
                        else -> "Solicitud inválida. Por favor, verifica la información ingresada."
                    }
                }
            }
            401 -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "Session expired or unauthorized. Please log in again."
                    IdiomaApp.CHINO -> "会话已过期或未授权。请重新登录。"
                    else -> "Tu sesión ha expirado o no estás autorizado. Inicia sesión nuevamente."
                }
            }
            403 -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "Access denied. You don't have permission for this action."
                    IdiomaApp.CHINO -> "访问被拒绝。您没有权限执行此操作。"
                    else -> "Acceso denegado. No tienes permisos para realizar esta acción."
                }
            }
            404 -> {
                cadenas.error404Mensaje
            }
            408, 504 -> {
                cadenas.errorRedMensaje
            }
            in 500..599 -> {
                cadenas.error50xMensaje
            }
            else -> {
                val extraido = extraerMensajeDeJson(cuerpoError)
                if (extraido != null && !esMensajeTecnico(extraido)) {
                    extraido
                } else {
                    cadenas.errorGeneralMensaje
                }
            }
        }
    }

    fun determinarCategoria(codigoHttp: Int?): CategoriaError {
        if (codigoHttp == null) return CategoriaError.GENERAL
        return when (codigoHttp) {
            404 -> CategoriaError.NO_ENCONTRADO
            in 500..599 -> CategoriaError.SERVIDOR
            408, 504 -> CategoriaError.RED
            401, 403 -> CategoriaError.SESION_EXPIRADA
            400 -> CategoriaError.SOLICITUD_INVALIDA
            else -> CategoriaError.GENERAL
        }
    }

    fun determinarCategoria(e: Throwable?): CategoriaError {
        if (e == null) return CategoriaError.GENERAL
        return if (esErrorDeRed(e)) CategoriaError.RED else CategoriaError.GENERAL
    }

    fun determinarCategoriaPorMensaje(mensaje: String?): CategoriaError {
        if (mensaje.isNullOrBlank()) return CategoriaError.GENERAL
        val lower = mensaje.lowercase()
        return when {
            lower.contains("encontrado") || lower.contains("found") || lower.contains("404") || lower.contains("recurso") -> CategoriaError.NO_ENCONTRADO
            lower.contains("mantenimiento") || lower.contains("servidor") || lower.contains("server") || lower.contains("50") -> CategoriaError.SERVIDOR
            lower.contains("conexión") || lower.contains("conexion") || lower.contains("red") || lower.contains("internet") || lower.contains("timeout") || lower.contains("tiempo de espera") || lower.contains("host") -> CategoriaError.RED
            lower.contains("sesión") || lower.contains("sesion") || lower.contains("autorizado") || lower.contains("credencial") -> CategoriaError.SESION_EXPIRADA
            else -> CategoriaError.GENERAL
        }
    }

    fun resolverDetallesVisuales(
        categoria: CategoriaError,
        mensajePersonalizado: String? = null,
        idioma: IdiomaApp = idiomaActual
    ): DetallesErrorVisual {
        val cadenas = CadenasIdiomas.obtener(idioma)
        return when (categoria) {
            CategoriaError.NO_ENCONTRADO -> DetallesErrorVisual(
                categoria = categoria,
                titulo = cadenas.error404Titulo,
                mensaje = mensajePersonalizado ?: cadenas.error404Mensaje,
                sugerencia = cadenas.error404Sugerencia,
                textoBotonPrincipal = cadenas.volverAlInicio,
                textoBotonSecundario = cadenas.irAExplorar
            )
            CategoriaError.SERVIDOR -> DetallesErrorVisual(
                categoria = categoria,
                titulo = cadenas.error50xTitulo,
                mensaje = mensajePersonalizado ?: cadenas.error50xMensaje,
                sugerencia = cadenas.error50xSugerencia,
                textoBotonPrincipal = cadenas.reintentar,
                textoBotonSecundario = cadenas.volverAlInicio
            )
            CategoriaError.RED -> DetallesErrorVisual(
                categoria = categoria,
                titulo = cadenas.errorRedTitulo,
                mensaje = mensajePersonalizado ?: cadenas.errorRedMensaje,
                sugerencia = cadenas.errorRedSugerencia,
                textoBotonPrincipal = cadenas.reintentar,
                textoBotonSecundario = cadenas.volverAlInicio
            )
            CategoriaError.SESION_EXPIRADA -> DetallesErrorVisual(
                categoria = categoria,
                titulo = cadenas.iniciarSesion,
                mensaje = mensajePersonalizado ?: cadenas.errorCredenciales,
                sugerencia = cadenas.errorGeneralSugerencia,
                textoBotonPrincipal = cadenas.iniciarSesion,
                textoBotonSecundario = cadenas.volverAlInicio
            )
            CategoriaError.SOLICITUD_INVALIDA, CategoriaError.GENERAL -> DetallesErrorVisual(
                categoria = categoria,
                titulo = cadenas.errorGeneralTitulo,
                mensaje = mensajePersonalizado ?: cadenas.errorGeneralMensaje,
                sugerencia = cadenas.errorGeneralSugerencia,
                textoBotonPrincipal = cadenas.reintentar,
                textoBotonSecundario = cadenas.volverAlInicio
            )
        }
    }

    private fun esErrorDeRed(e: Throwable): Boolean {
        if (e is UnknownHostException || e is SocketTimeoutException || e is ConnectException || e is SSLException) {
            return true
        }
        val mensaje = (e.message ?: "").lowercase()
        return mensaje.contains("unable to resolve host") ||
                mensaje.contains("no address associated with hostname") ||
                mensaje.contains("failed to connect") ||
                mensaje.contains("timeout") ||
                mensaje.contains("connection refused") ||
                mensaje.contains("network is unreachable") ||
                mensaje.contains("route to host") ||
                mensaje.contains("software caused connection abort")
    }

    private fun esErrorDeDatos(e: Throwable): Boolean {
        val nombre = e.javaClass.simpleName
        return nombre.contains("Json") || nombre.contains("Malformed") || nombre.contains("ParseException")
    }

    private fun esMensajeTecnico(mensaje: String): Boolean {
        val lower = mensaje.lowercase()
        return lower.contains("unable to resolve host") ||
                lower.contains("exception") ||
                lower.contains("java.") ||
                lower.contains("retrofit") ||
                lower.contains("okhttp") ||
                lower.contains("nullpointer") ||
                lower.contains("hostname") ||
                lower.contains("no address associated") ||
                lower.contains("failed to connect")
    }

    private fun extraerMensajeDeJson(jsonStr: String?): String? {
        if (jsonStr.isNullOrBlank()) return null
        return try {
            val json = JSONObject(jsonStr)
            val posiblesClaves = listOf("detail", "non_field_errors", "error", "mensaje", "message", "details")
            for (clave in posiblesClaves) {
                if (json.has(clave)) {
                    val valor = json.get(clave)
                    if (valor is JSONArray && valor.length() > 0) {
                        return valor.getString(0)
                    } else if (valor is String && valor.isNotBlank()) {
                        return valor
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun formatearErroresRegistro(jsonStr: String?, idioma: IdiomaApp): String? {
        if (jsonStr.isNullOrBlank()) return null
        return try {
            val json = JSONObject(jsonStr)
            val listaErrores = mutableListOf<String>()
            val iterador = json.keys()
            while (iterador.hasNext()) {
                val clave = iterador.next()
                val valor = json.get(clave)
                val primerMensaje = when (valor) {
                    is JSONArray -> if (valor.length() > 0) valor.getString(0) else ""
                    else -> valor.toString()
                }

                val campoTraducido = when (clave.lowercase()) {
                    "username" -> when (idioma) {
                        IdiomaApp.INGLES -> "Username"
                        IdiomaApp.CHINO -> "用户名"
                        else -> "Nombre de usuario"
                    }
                    "email" -> when (idioma) {
                        IdiomaApp.INGLES -> "Email"
                        IdiomaApp.CHINO -> "电子邮箱"
                        else -> "Correo electrónico"
                    }
                    "password" -> when (idioma) {
                        IdiomaApp.INGLES -> "Password"
                        IdiomaApp.CHINO -> "密码"
                        else -> "Contraseña"
                    }
                    "phone", "telefono" -> when (idioma) {
                        IdiomaApp.INGLES -> "Phone"
                        IdiomaApp.CHINO -> "电话"
                        else -> "Teléfono"
                    }
                    "first_name", "nombre" -> when (idioma) {
                        IdiomaApp.INGLES -> "First name"
                        IdiomaApp.CHINO -> "名字"
                        else -> "Nombre"
                    }
                    "last_name", "apellido" -> when (idioma) {
                        IdiomaApp.INGLES -> "Last name"
                        IdiomaApp.CHINO -> "姓氏"
                        else -> "Apellido"
                    }
                    else -> clave
                }

                val mensajeTraducido = traducirMensajeComun(primerMensaje, idioma) ?: primerMensaje
                if (mensajeTraducido.isNotBlank()) {
                    listaErrores.add("$campoTraducido: $mensajeTraducido")
                }
            }

            if (listaErrores.isNotEmpty()) {
                listaErrores.joinToString("\n")
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun traducirMensajeComun(mensaje: String, idioma: IdiomaApp): String? {
        val lower = mensaje.lowercase()
        return when {
            lower.contains("unable to log in with provided credentials") ||
            lower.contains("no active account found") ||
            lower.contains("invalid credentials") ||
            lower.contains("invalid username or password") -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "Incorrect username or password. Please verify your credentials."
                    IdiomaApp.CHINO -> "用户名或密码不正确。请核对您的凭据。"
                    else -> "Usuario o contraseña incorrectos. Por favor, verifica tus credenciales."
                }
            }
            lower.contains("already exists") || lower.contains("ya existe") -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "Already registered."
                    IdiomaApp.CHINO -> "已被注册。"
                    else -> "Ya se encuentra registrado."
                }
            }
            lower.contains("enter a valid email") || lower.contains("invalid email") -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "Enter a valid email address."
                    IdiomaApp.CHINO -> "请输入有效的电子邮箱地址。"
                    else -> "Ingresa un correo electrónico válido."
                }
            }
            lower.contains("this field is required") || lower.contains("este campo es requerido") -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "This field is required."
                    IdiomaApp.CHINO -> "此字段为必填项。"
                    else -> "Este campo es requerido."
                }
            }
            lower.contains("password is too short") || lower.contains("at least") -> {
                when (idioma) {
                    IdiomaApp.INGLES -> "Password must be at least 6 characters."
                    IdiomaApp.CHINO -> "密码长度至少需要 6 个字符。"
                    else -> "La contraseña debe tener al menos 6 caracteres."
                }
            }
            else -> null
        }
    }
}
