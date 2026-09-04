package com.example.codise

import com.example.codise.data.IdiomaApp
import com.example.codise.utils.CadenasIdiomas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class IdiomaTest {

    @Test
    fun testDesdeCodigo() {
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeCodigo("es"))
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeCodigo("es-ES"))
        assertEquals(IdiomaApp.INGLES, IdiomaApp.desdeCodigo("en"))
        assertEquals(IdiomaApp.INGLES, IdiomaApp.desdeCodigo("en-US"))
        assertEquals(IdiomaApp.CHINO, IdiomaApp.desdeCodigo("zh"))
        assertEquals(IdiomaApp.CHINO, IdiomaApp.desdeCodigo("zh-CN"))
        assertEquals(IdiomaApp.CHINO, IdiomaApp.desdeCodigo("zh-Hans"))
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeCodigo(null))
        assertEquals(IdiomaApp.ESPANOL, IdiomaApp.desdeCodigo("fr"))
    }

    @Test
    fun testCabeceraAcceptLanguageSegunGuiaApi() {
        // Validación con GUIA_CONEXION_API.md sección 1.2:
        // Inglés: Accept-Language: en
        // Mandarín: Accept-Language: zh-CN o zh
        // Español: Accept-Language: es
        assertEquals("es", IdiomaApp.ESPANOL.cabeceraHttp)
        assertEquals("en", IdiomaApp.INGLES.cabeceraHttp)
        assertEquals("zh-CN", IdiomaApp.CHINO.cabeceraHttp)
    }

    @Test
    fun testCadenasCompletas() {
        IdiomaApp.entries.forEach { idioma ->
            val textos = CadenasIdiomas.obtener(idioma)
            assertNotNull(textos.inicio)
            assertNotNull(textos.circuitos)
            assertNotNull(textos.eventos)
            assertNotNull(textos.mural)
            assertNotNull(textos.perfil)
            assertNotNull(textos.asistenteTitulo)
            assertNotNull(textos.seleccionarIdioma)
            assertNotNull(textos.cerrarSesion)
        }
    }
}
