package com.example.codise.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.codise.R

// Sistema tipográfico completo según especificación (Tipografias.pdf)
// Lora (Serif): Transmite historia, elegancia y la mística de un manuscrito antiguo,
// conectando la interfaz con las raíces de los códices.
val LoraFontFamily = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_medium, FontWeight.Medium),
    Font(R.font.lora_bold, FontWeight.Bold)
)

// Roboto (Sans-Serif): Garantiza una lectura cómoda, moderna y clara en pantallas móviles
// para textos extensos, botones y menús.
val RobotoFontFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

// 1. Titular Principal
// Fuente: Lora | Peso: Bold (700) | Tamaño: 30px | Interlineado: 36px | Aplicación: Títulos principales de pantalla
val TitularPrincipal = TextStyle(
    fontFamily = LoraFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 36.sp
)

// 2. Subtítulo (H2)
// Fuente: Lora | Peso: Medium (500) | Tamaño: 22px | Interlineado: 28px | Aplicación: Nombres de ciudades y Categorías
val SubtituloH2 = TextStyle(
    fontFamily = LoraFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    lineHeight = 28.sp
)

// 3. Cuerpo de texto
// Fuente: Roboto | Peso: Regular (400) | Tamaño: 15px | Interlineado: 22px | Aplicación: Descripciones de eventos, párrafos
val CuerpoTexto = TextStyle(
    fontFamily = RobotoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
)

// 4. Botones
// Fuente: Roboto | Peso: Bold (700) | Tamaño: 15px | Interlineado: 18px | Aplicación: Texto dentro de botones
val TextoBoton = TextStyle(
    fontFamily = RobotoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    lineHeight = 18.sp
)

// 5. Leyenda / Fechas
// Fuente: Roboto | Peso: Regular (400) | Tamaño: 12px | Interlineado: 16px | Aplicación: Datos secundarios como (Hora, ubicaciones y fechas)
val LeyendaFechas = TextStyle(
    fontFamily = RobotoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

// Sistema de tipografía de Material 3 completo alineado a la especificación
val Typography = Typography(
    // Titulares principales (Lora Bold)
    displayLarge = TitularPrincipal.copy(fontSize = 36.sp, lineHeight = 42.sp),
    displayMedium = TitularPrincipal.copy(fontSize = 32.sp, lineHeight = 38.sp),
    displaySmall = TitularPrincipal,
    headlineLarge = TitularPrincipal,
    headlineMedium = TitularPrincipal.copy(fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = SubtituloH2,

    // Subtítulos y categorías (Lora Medium)
    titleLarge = SubtituloH2,
    titleMedium = SubtituloH2,
    titleSmall = SubtituloH2.copy(fontSize = 18.sp, lineHeight = 24.sp),

    // Cuerpo de texto (Roboto Regular)
    bodyLarge = CuerpoTexto,
    bodyMedium = CuerpoTexto,
    bodySmall = LeyendaFechas,

    // Botones y etiquetas (Roboto Bold / Regular)
    labelLarge = TextoBoton,
    labelMedium = TextoBoton.copy(fontSize = 13.sp, lineHeight = 16.sp),
    labelSmall = LeyendaFechas
)