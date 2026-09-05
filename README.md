<div align="center">

```
  ____   ___   ____   ___   ____  _____ _     _   _ 
 / ___| / _ \ |  _ \ |_ _| / ___|| ____| |   | | | |
| |    | | | || | | | | | | |    |  _| | |   | | | |
| |___ | |_| || |_| | | | | |___ | |___| |___| |_| |
 \____| \___/ |____/ |___| \____||_____|_____|\___/ 

      App Móvil Android - Ciudades Creativas de Nicaragua
```

# Codice路 (Android)

**Aplicación móvil nativa en Kotlin y Jetpack Compose para la Red Nacional de Ciudades Creativas y Turismo Cultural**

[![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26--35)-brightgreen.svg)](https://developer.android.com/)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-v2.2.10-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Compose_BOM-2026.02.01-blue.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material_3-Enabled-teal.svg)](https://m3.material.io/)
[![AGP Version](https://img.shields.io/badge/AGP-v9.3.1-darkblue.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Room Database](https://img.shields.io/badge/Room-v2.8.4_(KSP)-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Retrofit](https://img.shields.io/badge/Retrofit-v2.9.0-red.svg)](https://square.github.io/retrofit/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF.svg)](.github/workflows/publicar-apk.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Release](https://img.shields.io/badge/Release-codise.apk-success.svg)](https://github.com/)

---

</div>

## Tabla de Contenidos

- [1. Descripcion General](#1-descripcion-general)
- [2. Arquitectura de la Aplicacion Movil](#2-arquitectura-de-la-aplicacion-movil)
  - [2.1. Diagrama de Capas de la Aplicacion](#21-diagrama-de-capas-de-la-aplicacion)
  - [2.2. Flujo de Navegacion y Ciclo de Estados](#22-flujo-de-navegacion-y-ciclo-de-estados)
  - [2.3. Autenticacion Hibrida (JWT Nativo + Google Sign-In con Credential Manager)](#23-autenticacion-hibrida-jwt-nativo--google-sign-in-con-credential-manager)
  - [2.4. Motor Geografico: Croquis Vectorial Canvas y Validacion GPS Haversine](#24-motor-geografico-croquis-vectorial-canvas-y-validacion-gps-haversine)
  - [2.5. Sistema de Asistente Virtual Inteligente con Contexto Espacial ("Eduardo")](#25-sistema-de-asistente-virtual-inteligente-con-contexto-espacial-eduardo)
  - [2.6. Sistema de Notificaciones Exactas y Alarmas de Eventos (AlarmManager)](#26-sistema-de-notificaciones-exactas-y-alarmas-de-eventos-alarmmanager)
- [3. Estructura Modular del Proyecto](#3-estructura-modular-del-proyecto)
  - [3.1. Arbol de Directorios](#31-arbol-de-directorios)
  - [3.2. Responsabilidad de Paquetes y Componentes](#32-responsabilidad-de-paquetes-y-componentes)
- [4. Dependencias y Tecnologias](#4-dependencias-y-tecnologias)
  - [4.1. Entorno de Ejecucion y Compilacion](#41-entorno-de-ejecucion-y-compilacion)
  - [4.2. Librerias de UI y Jetpack Compose](#42-librerias-de-ui-y-jetpack-compose)
  - [4.3. Networking, Persistencia y Servicios de Google](#43-networking-persistencia-y-servicios-de-google)
- [5. Configuracion y Conectividad con la API Backend](#5-configuracion-y-conectividad-con-la-api-backend)
  - [5.1. Parametros de Red y Base URL](#51-parametros-de-red-y-base-url)
  - [5.2. Interceptores de Red, Cache y Manejo de Sesion](#52-interceptores-de-red-cache-y-manejo-de-sesion)
  - [5.3. Soporte Multilingüe Dinamico (Español, Ingles, Chino Mandarin)](#53-soporte-multilingüe-dinamico-español-ingles-chino-mandarin)
- [6. Compilacion, Ejecucion y Distribucion](#6-compilacion-ejecucion-y-distribucion)
  - [6.1. Requisitos Previos del Sistema](#61-requisitos-previos-del-sistema)
  - [6.2. Comandos de Compilacion con Gradle Wrapper](#62-comandos-de-compilacion-con-gradle-wrapper)
  - [6.3. Instalacion y Depuracion con ADB](#63-instalacion-y-depuracion-con-adb)
  - [6.4. Pipeline de CI/CD y Generacion de APK (GitHub Actions)](#64-pipeline-de-cicd-y-generacion-de-apk-github-actions)
- [7. Catalogo de Pantallas y Funcionalidades](#7-catalogo-de-pantallas-y-funcionalidades)
  - [7.1. Inicio de Sesion y Registro (Nativo & Google Sign-In)](#71-inicio-de-sesion-y-registro-nativo--google-sign-in)
  - [7.2. Croquis Interactivo de Nicaragua (Canvas 2D con Zoom & Pan)](#72-croquis-interactivo-de-nicaragua-canvas-2d-con-zoom--pan)
  - [7.3. Ciudades Creativas, Circuitos y Checklist de Puntos de Interes](#73-ciudades-creativas-circuitos-y-checklist-de-puntos-de-interes)
  - [7.4. Mural de Eventos, Reacciones "Grano de Cafe" y Notificaciones](#74-mural-de-eventos-reacciones-grano-de-cafe-y-notificaciones)
  - [7.5. Feed Social, Carga Masiva de Fotografias y Comentarios](#75-feed-social-carga-masiva-de-fotografias-y-comentarios)
  - [7.6. Perfil de Usuario y Registro de Emprendimientos (Protagonistas)](#76-perfil-de-usuario-y-registro-de-emprendimientos-protagonistas)
  - [7.7. Asistente Turistico IA "Eduardo" (Chatbot Georreferenciado)](#77-asistente-turistico-ia-eduardo-chatbot-georreferenciado)
- [8. Permisos de la Aplicacion (AndroidManifest.xml)](#8-permisos-de-la-aplicacion-androidmanifestxml)
- [9. Estrategia de Ramas y Control de Versiones](#9-estrategia-de-ramas-y-control-de-versiones)
- [10. Contribucion](#10-contribucion)
- [11. Autores y Agradecimientos](#11-autores-y-agradecimientos)
- [12. Licencia](#12-licencia)

---

## 1. Descripcion General

**Codice路 (Android)** es la aplicación móvil oficial de la Red Nacional de Ciudades Creativas y Turismo Cultural de Nicaragua. Construida desde cero con **Kotlin** y **Jetpack Compose (Material 3)**, proporciona una experiencia inmersiva para turistas nacionales, internacionales y emprendedores locales (protagonistas).

La aplicación permite descubrir, navegar y experimentar la identidad cultural y el patrimonio de las **10 Ciudades Creativas de Nicaragua**:

1. **Estelí** - *Capital del Muralismo y Cuna del Tabaco*
2. **León** - *Primera Capital de la Revolución y Ciudad Universitaria*
3. **Nagarote** - *Municipio Azul, Gastronomía y Tradición del Quesillo*
4. **Managua** - *Capital Creativa y Centro Histórico Cultural*
5. **Masaya** - *Cuna del Folclore Nicaragüense y Artesanías Nacionales*
6. **Granada** - *Gran Sultana y Joya de la Arquitectura Colonial*
7. **San Juan de Oriente** - *Cuna de la Cerámica Precolombina y Contemporánea*
8. **Juigalpa** - *Tierra de Caracoles y Tradición Ganadera Chontaleña*
9. **Matagalpa** - *Perla del Septentrión y Capital del Café de Altura*
10. **Bluefields** - *Corazón Multicultural del Caribe y Cuna del Palo de Mayo*

La app interactúa directamente con el backend REST de **Codice路 Core** (desplegado en la nube), ofreciendo navegación territorial mediante un mapa croquis interactivo renderizado en Canvas, validación física de visitas a puntos de interés por GPS (fórmula de Haversine), agenda cultural de eventos con recordatorios del sistema, feed social interactivo con subida múltiple de fotografías, perfiles de negocio para protagonistas y un asistente conversacional turístico con inteligencia artificial ("Eduardo").

---

## 2. Arquitectura de la Aplicacion Movil

### 2.1. Diagrama de Capas de la Aplicacion

La arquitectura sigue el patrón **MVVM (Model-View-ViewModel)** y los principios de **Arquitectura Limpia (Clean Architecture)** recomendados por Google Android, manteniendo un flujo de datos unidireccional (UDF - *Unidirectional Data Flow*):

```text
+-------------------------------------------------------------------------+
|                        CAPA DE PRESENTACION (UI)                        |
|   - Jetpack Compose (Declarativa, Material 3, Edge-to-Edge)             |
|   - Composables: CroquisNicaragua, Pantallas de Ciudades, Eventos,      |
|     Mural Social, Perfil, Chat Asistente, Diálogos y Modales            |
|   - Estados de UI reactivos observados mediante collectAsState()        |
+------------------------------------+------------------------------------+
                                     | Eventos de Usuario (Intentions)
                                     v
+-------------------------------------------------------------------------+
|                        CAPA DE VIEWMODELS (State)                       |
|   - ViewModelPrincipal, ViewModelEventos, ViewModelPublicaciones,       |
|     ViewModelPerfil, ViewModelLogin, ViewModelAsistente                 |
|   - Manejo de ciclo de vida con AndroidX Lifecycle & viewModelScope     |
|   - Emisión de Estados: StateFlow<EstadoUi>, MutableState, Coroutines   |
+-------------------+--------------------------------+--------------------+
                    |                                |
                    v                                v
+------------------------------------+   +--------------------------------+
|    CAPA DE DATOS REMOTA (Network)  |   |    CAPA DE PERSISTENCIA LOCAL  |
|   Retrofit 2 + OkHttp 3            |   |   Room Database (SQLite)       |
|   - InterceptorIdioma (I18n)       |   |   - BaseDeDatosApp (Room)      |
|   - InterceptorAutenticacion (JWT) |   |   - PuntoVisitadoDao           |
|   - OkHttp Cache (5 MB)            |   |   - Tabla 'visited_pois'       |
|   - Conversión Gson / Multipart    |   |   SharedPreferences (Session)  |
+-------------------+----------------+   +--------------------------------+
                    |                                |
                    v                                v
+------------------------------------+   +--------------------------------+
|          API REST BACKEND          |   |       HARDWARE & SERVICIOS     |
|   Codice路 Core (Django 6.0 + DRF) |   |   - Google Play Services GPS   |
|   - Autenticación, Ciudades,       |   |   - Android Credential Manager |
|     Eventos, Feed, Asistente IA    |   |   - AlarmManager & Notif.      |
+------------------------------------+   +--------------------------------+
```

---

### 2.2. Flujo de Navegacion y Ciclo de Estados

El estado de navegación de alto nivel se gestiona de forma centralizada en `MainActivity.kt` sincronizado con el componente `BarraNavegacionInferior`:

```text
                       +-------------------------+
                       |  PantallaLogin (Auth)   |
                       +------------+------------+
                                    | (Login exitoso / Google Auth)
                                    v
                       +-------------------------+
                       |  AplicacionAutenticada  |
                       +------------+------------+
                                    |
     +-----------------+------------+------------+-----------------+
     |                 |                         |                 |
     v                 v                         v                 v
[ Inicio (Main) ] [ Circuitos/POI ]       [ Eventos/Mural ] [ Perfil/Empresas ]
     │                 │                         │                 │
     ├─ Croquis Canvas ├─ Lista Circuitos        ├─ Lista Eventos  ├─ Datos Usuario
     │  Interactivo    ├─ Detalle Circuito       ├─ Detalle Evento ├─ Selector Idioma
     ├─ Lista Ciudades ├─ Puntos de Interés      ├─ Subir Evento   ├─ Mis Empresas
     └─ Detalle Ciudad └─ Checklist GPS          ├─ Reacc. Granos  └─ Registrar Negocio
                                                 └─ Notif. Exacta
                       [ Feed Social ]           [ Asistente IA ]
                             │                         │
                             ├─ Múltiples Fotos        ├─ Chatbot "Eduardo"
                             ├─ Likes & Comentarios    ├─ Prompt con GPS
                             └─ Subir Publicación      └─ Sugerencias
```

---

### 2.3. Autenticacion Hibrida (JWT Nativo + Google Sign-In con Credential Manager)

El sistema provee un mecanismo dual y seguro de acceso:

1. **Credenciales Estándar (JWT Nativo)**:
   - Registro e inicio de sesión contra `/api/auth/register/` y `/api/auth/login/`.
   - Almacenamiento seguro del par de tokens (`access` y `refresh`) mediante `AdministradorSesion`.
   - Inyección automática en cabeceras de red mediante `InterceptorAutenticacion`:
     ```http
     Authorization: Bearer <access_token>
     ```

2. **Google Sign-In Moderno (Android Credential Manager API)**:
   - Implementado en `AutenticadorGoogle.kt` utilizando las librerías `androidx.credentials` y `googleid`.
   - Solicita de forma nativa el token de identidad criptográfico (`idToken`) de Google vinculándolo al `google_web_client_id`.
   - Intercambio directo con el backend mediante `POST /api/auth/google/` para obtener sesión JWT propia del ecosistema Codice路.

---

### 2.4. Motor Geografico: Croquis Vectorial Canvas y Validacion GPS Haversine

#### 2.4.1. Croquis Vectorial Interactivo de Nicaragua (`CroquisNicaragua.kt`)
En lugar de depender exclusivamente de mapas pesados de terceros, la aplicación cuenta con un croquis geográfico vectorial de alta precisión dibujado en tiempo real con Compose `Canvas`:
- **Geometría Geodésica**: Matriz de coordenadas que dibuja el perímetro nacional exacto (Península de Cosigüina, Río Coco, Costa Caribe, Río San Juan, Península de Rivas y Golfo de Fonseca).
- **Lagos de Nicaragua**: Renderizado de los dos grandes cuerpos de agua (Lago Cocibolca y Lago Xolotlán).
- **Proyección Cartográfica**: Conversión de coordenadas geográficas (Lat/Lng) a píxeles de pantalla aplicando factor de aspecto geodésico ($\cos(12.5^\circ) \approx 0.976$).
- **Gestos Táctiles Avanzados**: Soporte fluido para arrastre panorámico (*Pan*), pellizco para acercar/alejar (*Pinch-to-zoom* con límites $1.0\times$ a $5.0\times$) y doble toque para restablecer vista.
- **Pines Interactivos**: Marcadores interactivos animados para las 10 Ciudades Creativas con tarjeta flotante de vista previa y acceso directo a sus circuitos.

#### 2.4.2. Validación de Proximidad GPS y Haversine
Al recorrer un circuito cultural, el turista puede validar físicamente su visita a cada Punto de Interés:
1. `MainActivity.kt` solicita la posición satelital en alta precisión vía `FusedLocationProviderClient`.
2. Las coordenadas del usuario son enviadas a `POST /api/visitas/`.
3. El backend calcula la distancia ortodrómica con la fórmula de Haversine:
   - Si la distancia $d \le 200.0 \text{ metros}$, la visita queda validada oficialmente en el sistema.
4. **Persistencia Offline con Room**: La app guarda simultáneamente el estado de visitado en la base de datos local `BaseDeDatosApp` (`visited_pois`), garantizando que la interfaz refleje el progreso aun cuando la conectividad a internet sea intermitente.

---

### 2.5. Sistema de Asistente Virtual Inteligente con Contexto Espacial ("Eduardo")

El asistente virtual **"Eduardo"** (`PantallaAsistente.kt` / `ViewModelAsistente.kt`) es un guía cultural con inteligencia artificial integrado en tiempo real:
- **Contexto Geográfico**: Permite alternar la inyección de la posición GPS del usuario en el payload (`UbicacionGps(lat, lng)`). De esta forma, el asistente adapta sus sugerencias al lugar donde se encuentra físicamente el turista.
- **Historial Conversacional Dinámico**: Mantiene la memoria del diálogo transmitiendo los turnos previos (`user` y `model`) a `/api/asistente/chat/`.
- **Soporte Multilingüe Automático**: El asistente responde fluidamente en el idioma seleccionado por el usuario (Español, Inglés o Chino Mandarín).
- **Chips de Preguntas Rápidas**: Accesos directos para consultas frecuentes sobre circuitos, gastronomía, eventos de temporada y leyendas locales.

---

### 2.6. Sistema de Notificaciones Exactas y Alarmas de Eventos (AlarmManager)

Para maximizar la asistencia a actividades culturales y cumplir con las políticas de batería de Android:
- **Recordatorios Programados**: Utiliza `AlarmManager` con `SCHEDULE_EXACT_ALARM` y `PendingIntent` inmutable.
- **Receptor en Segundo Plano**: `ReceptorNotificacionesEvento` recibe el disparo de la alarma y delega en `AyudanteNotificaciones`.
- **Canal de Notificaciones**: Canal dedicado `event_notifications` con soporte para Android 8.0+ (Oreo) y validación de permisos en tiempo de ejecución para Android 13+ (`POST_NOTIFICATIONS`).
- **Acción Integrada "Llegar"**: Las notificaciones incorporan un botón de acción rápida que abre la aplicación de navegación (Google Maps / Waze) con las coordenadas exactas del evento.

---

## 3. Estructura Modular del Proyecto

### 3.1. Arbol de Directorios

```text
Codise/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/codise/
│   │   │   │   ├── data/                          # Capa de datos y modelos
│   │   │   │   │   ├── Ciudad.kt                  # Modelos de Ciudad, Circuito, Punto de Interés
│   │   │   │   │   ├── Evento.kt                  # Modelo de Evento cultural y solicitudes
│   │   │   │   │   ├── GestorIdioma.kt            # Gestor singleton de I18n y SharedPreferences
│   │   │   │   │   ├── GestorSesion.kt            # Administrador de sesión y tokens JWT
│   │   │   │   │   ├── InterceptorAutenticacion.kt# Interceptor OkHttp para cabecera Bearer
│   │   │   │   │   ├── InterceptorIdioma.kt       # Interceptor OkHttp para cabecera Accept-Language
│   │   │   │   │   ├── Negocio.kt                 # Modelo de Empresa / Emprendimiento local
│   │   │   │   │   ├── Publicacion.kt             # Modelo de Publicación social y Comentarios
│   │   │   │   │   ├── ServicioApi.kt             # Definición Retrofit 2 de endpoints REST
│   │   │   │   │   ├── Usuario.kt                 # Modelo de Usuario y respuestas de sesión
│   │   │   │   │   ├── Visita.kt                  # Modelos de solicitud y respuesta de Visita GPS
│   │   │   │   │   └── local/                     # Persistencia local con Room
│   │   │   │   │       ├── BaseDatosApp.kt        # RoomDatabase de la aplicación
│   │   │   │   │       ├── PuntoInteresVisitado.kt# Entidad Room 'visited_pois'
│   │   │   │   │       └── PuntoInteresVisitadoDao.kt # DAO para consultas y modificaciones
│   │   │   │   ├── receivers/                     # Receptores de difusión (BroadcastReceivers)
│   │   │   │   │   └── ReceptorNotificacionesEvento.kt # Receptor de alarmas para notificaciones
│   │   │   │   ├── ui/theme/                      # Sistema de diseño y temas Compose
│   │   │   │   │   ├── Color.kt                   # Paleta oficial (Azul Petróleo, Celeste, Dorado)
│   │   │   │   │   ├── Theme.kt                   # Tema Material 3 claro y oscuro
│   │   │   │   │   └── Type.kt                    # Tipografía y estilos tipográficos
│   │   │   │   ├── utils/                         # Utilidades y funciones de extensión
│   │   │   │   │   ├── AutenticadorGoogle.kt      # Integración con Google Credential Manager
│   │   │   │   │   ├── AyudanteNotificaciones.kt  # Construcción de notificaciones y canales
│   │   │   │   │   ├── CadenasIdiomas.kt          # Diccionarios de textos (ES, EN, ZH)
│   │   │   │   │   ├── Extensiones.kt             # Extensiones de Kotlin y helpers de URLs
│   │   │   │   │   ├── UtilidadesCompartir.kt     # Intents del sistema para compartir contenido
│   │   │   │   │   ├── UtilidadesRed.kt           # Verificación de conectividad a internet
│   │   │   │   │   └── UtilidadesUbicacion.kt     # Parser de URLs Maps, DMS, Geocoder y GPS
│   │   │   │   ├── CarruselGaleria.kt             # Visualizador de carrusel de fotos
│   │   │   │   ├── CroquisNicaragua.kt            # Renderizador Canvas 2D del mapa de Nicaragua
│   │   │   │   ├── DialogoSeleccionIdioma.kt      # Modal selector de idioma (ES / EN / ZH)
│   │   │   │   ├── DialogoVistaPreviaImagen.kt    # Diálogo de ampliación de fotografías
│   │   │   │   ├── EventosViewModel.kt            # ViewModel de eventos, filtros y asistencias
│   │   │   │   ├── InicioSesionViewModel.kt       # ViewModel de login, registro y Google Auth
│   │   │   │   ├── MainActivity.kt                # Actividad principal y orquestador de UI
│   │   │   │   ├── PantallaAsistente.kt           # UI del chat con el asistente virtual Eduardo
│   │   │   │   ├── PantallaCircuitosYPuntosInteres.kt # Explorador de rutas y atractivos
│   │   │   │   ├── PantallaDetalleCircuito.kt     # Detalle de ruta, mapa y checklist de visita
│   │   │   │   ├── PantallaDetalleCiudad.kt       # Perfil completo de ciudad y datos históricos
│   │   │   │   ├── PantallaDetalleEvento.kt       # Información de evento y recordatorio
│   │   │   │   ├── PantallaEventos.kt             # Cartelera cultural y mural de eventos
│   │   │   │   ├── PantallaInicioSesion.kt        # Formularios de acceso y registro
│   │   │   │   ├── PantallaPerfil.kt              # Gestión de cuenta y registro de empresas
│   │   │   │   ├── PantallaPublicaciones.kt       # Feed social con likes y comentarios
│   │   │   │   ├── PantallaSubirEvento.kt         # Formulario de alta de eventos (Protagonistas)
│   │   │   │   ├── PantallaSubirPublicacion.kt    # Carga de posts con fotos múltiples
│   │   │   │   ├── PerfilViewModel.kt             # ViewModel de perfil y empresas vinculadas
│   │   │   │   ├── PrincipalViewModel.kt          # ViewModel de ciudades, circuitos y visitas
│   │   │   │   ├── PublicacionesViewModel.kt      # ViewModel del feed social y likes
│   │   │   │   └── ViewModelAsistente.kt          # ViewModel de interacción con IA Eduardo
│   │   │   ├── res/                               # Recursos Android (Strings, Iconos, XML)
│   │   │   │   ├── drawable/                      # Vector drawables (logo, Google icon)
│   │   │   │   ├── mipmap-*/                      # Iconos de la aplicación en varias densidades
│   │   │   │   ├── values/                        # Colores, temas y strings base (Español)
│   │   │   │   ├── values-en/                     # Strings localizados en Inglés
│   │   │   │   ├── values-zh/                     # Strings localizados en Chino Mandarín
│   │   │   │   └── xml/                           # FileProvider paths y reglas de backup
│   │   │   └── AndroidManifest.xml                # Declaración de componentes, permisos y tema
│   │   └── test/ & androidTest/                   # Pruebas unitarias e instrumentadas
│   └── build.gradle.kts                           # Configuración de compilación del módulo :app
├── gradle/
│   ├── libs.versions.toml                         # Catálogo central de versiones de dependencias
│   └── wrapper/                                   # Gradle Wrapper binario y configuración
├── .github/
│   └── workflows/
│       └── publicar-apk.yml                       # Flujo automatizado de CI/CD para release de APK
├── build.gradle.kts                               # Configuración raíz de Gradle
├── settings.gradle.kts                            # Declaración de repositorios y módulos
├── gradle.properties                              # Propiedades de la JVM y banderas de Gradle
├── GUIA_CONTROL_VERSIONES.md                      # Estándar de ramas Git y Conventional Commits
└── README.md                                      # Documentación técnica principal del proyecto
```

---

### 3.2. Responsabilidad de Paquetes y Componentes

- **`data/`**: Define los contratos de datos (Data Classes), servicios Retrofit (`ServicioApi`) y gestores de persistencia liviana (`GestorSesion`, `GestorIdioma`).
- **`data/local/`**: Implementa el almacenamiento SQLite local a través de Room (`BaseDeDatosApp`), aislando el acceso a datos de visitas fuera de línea mediante `PuntoVisitadoDao`.
- **`ui/theme/`**: Centraliza la identidad de marca, paleta de colores institucional (`AzulPetroleo`, `Celeste`, `GoldColor`) y estilos de Material 3 con adaptación para barra de navegación y barra de estado translúcidas.
- **`utils/`**: Provee utilidades transversales para manejo de geolocalización satelital, análisis de enlaces de Google Maps, autenticación con Credential Manager y diccionarios multilingües.
- **`receivers/`**: Procesa intenciones del sistema en segundo plano sin requerir que la interfaz de usuario esté abierta.

---

## 4. Dependencias y Tecnologias

### 4.1. Entorno de Ejecucion y Compilacion

- **Lenguaje**: Kotlin `v2.2.10`
- **Gradle**: `v8.x`
- **Android Gradle Plugin (AGP)**: `v9.3.1`
- **KSP (Kotlin Symbol Processing)**: `v2.3.6`
- **Java Compatibility**: OpenJDK 11 / 21 (Bytecode compatible con Java 11)
- **Compile SDK**: `35` (Android 15)
- **Target SDK**: `35`
- **Min SDK**: `26` (Android 8.0 Oreo - cobertura > 95% del ecosistema)

---

### 4.2. Librerias de UI y Jetpack Compose

Declaradas en el catálogo de versiones `gradle/libs.versions.toml`:

| Libreria | Version / BOM | Proposito en la Aplicacion |
| :--- | :--- | :--- |
| `androidx-compose-bom` | `2026.02.01` | Sincronización determinística de versiones de Jetpack Compose. |
| `androidx-compose-material3` | BOM | Componentes de diseño Material Design 3. |
| `androidx-compose-material-icons-extended` | BOM | Catálogo completo de íconos vectoriales de Material. |
| `androidx-activity-compose` | `1.8.0` | Integración de Activity con Composable y `BackHandler`. |
| `androidx-lifecycle-viewmodel-compose` | `2.6.1` | Vinculación de ViewModels con el árbol de componentes Compose. |
| `coil-compose` | `2.7.0` | Carga asíncrona, decodificación y almacenamiento en caché de imágenes. |
| `youtube-player` | `13.0.0` | Reproductor de videos nativo de YouTube para galerías multimedia. |

---

### 4.3. Networking, Persistencia y Servicios de Google

| Libreria | Version | Proposito en la Aplicacion |
| :--- | :--- | :--- |
| `retrofit` | `2.9.0` | Cliente HTTP REST declarativo para conexión con el backend. |
| `retrofit-gson` | `2.9.0` | Conversión y serialización bidireccional JSON. |
| `room-runtime` & `room-ktx` | `2.8.4` | Abstracción de base de datos local SQLite con soporte para Coroutines Flow. |
| `play-services-location` | `21.4.0` | Fused Location Provider para geoposicionamiento satelital de alta precisión. |
| `androidx-credentials` | `1.5.0` | Android Credential Manager para gestión de identidad unificada. |
| `googleid` | `1.1.1` | Especificación de credenciales Google ID Token para Credential Manager. |

---

## 5. Configuracion y Conectividad con la API Backend

### 5.1. Parametros de Red y Base URL

La aplicación se comunica con el backend oficial de **Codice路 Core**. La URL base se configura centralizadamente en `com.example.codise.data.ServicioApi`:

```kotlin
companion object {
    // Servidor de Producción (Railway Cloud)
    const val URL_BASE = "https://codisecore-production.up.railway.app/"
    
    // Para desarrollo local contra emulador Android:
    // const val URL_BASE = "http://10.0.2.2:8000/"
    
    // Para desarrollo local contra dispositivo físico en la misma red WiFi:
    // const val URL_BASE = "http://192.168.1.X:8000/"
}
```

> ⚠️ **Nota para Entornos de Desarrollo HTTP:** `AndroidManifest.xml` incluye la bandera `android:usesCleartextTraffic="true"` para facilitar pruebas locales sin certificados SSL en etapas de desarrollo. En producción, la comunicación se realiza exclusivamente bajo HTTPS.

---

### 5.2. Interceptores de Red, Cache y Manejo de Sesion

El cliente `OkHttpClient` implementa una canalización robusta de solicitudes:

1. **`InterceptorAutenticacion`**: Extrae la sesión persistida desde `AdministradorSesion` e inyecta dinámicamente el encabezado `Authorization: Bearer <token>` cuando existe un usuario autenticado.
2. **`InterceptorIdioma`**: Extrae el código de idioma activo desde `GestorIdioma` e inyecta el encabezado estándar `Accept-Language` (`es`, `en`, `zh-CN`) en cada llamada HTTP para que el backend entregue contenidos traducidos.
3. **Caché HTTP en Memoria/Disco**: Cuenta con un pool de caché OkHttp de **5 MB** en el directorio de caché de la app, permitiendo navegación fluida ante pérdidas temporales de señal.
4. **Timeouts Extendidos para Carga Masiva**:
   - `connectTimeout`: 60 segundos.
   - `writeTimeout`: 120 segundos (adecuado para subir lotes de hasta 10 fotos en alta resolución).
   - `readTimeout`: 60 segundos.

---

### 5.3. Soporte Multilingüe Dinamico (Español, Ingles, Chino Mandarin)

La aplicación implementa internacionalización dinámica (I18n) tanto en la interfaz local como en las respuestas remotas:

| Idioma | Código | Etiqueta Nativa | Cabecera HTTP `Accept-Language` | Bandera |
| :--- | :---: | :---: | :---: | :---: |
| **Español** | `es` | Español | `es` | 🇳🇮 |
| **Inglés** | `en` | English | `en` | 🇺🇸 |
| **Chino Mandarín** | `zh` | 中文 (简体) | `zh-CN` | 🇨🇳 |

El usuario puede alternar el idioma en cualquier momento desde la barra superior o desde su perfil sin reiniciar la aplicación. El cambio actualiza de forma reactiva el `CompositionLocalProvider(LocalCadenas)` de Jetpack Compose, invalida la caché de red y solicita los datos traducidos al backend.

---

## 6. Compilacion, Ejecucion y Distribucion

### 6.1. Requisitos Previos del Sistema

- **Android Studio**: Ladybug / Meerkat (2024.2+) o superior con Android SDK Platform 35 instalado.
- **Java Development Kit (JDK)**: JDK 17 o JDK 21 (recomendado Azul Zulu JDK 21).
- **Dispositivo Físico o Emulador**: Android 8.0 (API 26) o superior con Google Play Services instalado.

---

### 6.2. Comandos de Compilacion con Gradle Wrapper

#### Otorgar permisos de ejecución (Linux / macOS)
```bash
chmod +x gradlew
```

#### Compilar APK en modo Debug
```bash
./gradlew assembleDebug --stacktrace
```
*El binario generado se ubicará en:* `app/build/outputs/apk/debug/app-debug.apk`

#### Compilar APK en modo Release
```bash
./gradlew assembleRelease
```
*El binario generado se ubicará en:* `app/build/outputs/apk/release/app-release-unsigned.apk`

#### Ejecutar pruebas unitarias locales
```bash
./gradlew test
```

#### Limpiar artefactos generados
```bash
./gradlew clean
```

---

### 6.3. Instalacion y Depuracion con ADB

Con el dispositivo físico conectado por USB (o emulador activo) y la depuración USB habilitada:

#### Listar dispositivos conectados
```bash
adb devices
```

#### Instalar el APK Debug compilado
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Iniciar la aplicación inmediatamente
```bash
adb shell am start -n com.example.codise/.MainActivity
```

#### Inspeccionar logs filtrados de la aplicación
```bash
adb logcat -s "Codice" "Retrofit" "AndroidRuntime"
```

---

### 6.4. Pipeline de CI/CD y Generacion de APK (GitHub Actions)

El proyecto cuenta con integración y entrega continua automatizada en `.github/workflows/publicar-apk.yml`:

- **Disparador**: Cada `push` a la rama `master` o ejecución manual vía `workflow_dispatch`.
- **Entorno de Compilación**: Contenedor `ubuntu-latest` con JDK 21 (distribución Zulu) y caché automática de dependencias de Gradle.
- **Artefacto de Salida**: Compila automáticamente el APK mediante `./gradlew assembleDebug`, lo renombra a `codise.apk` y publica una Release con la etiqueta `latest` en GitHub con acceso público directo para descarga.

---

## 7. Catalogo de Pantallas y Funcionalidades

### 7.1. Inicio de Sesion y Registro (Nativo & Google Sign-In)

- **Formulario Nativo**: Registro de nuevos usuarios con selección explícita de rol (**Turista** o **Protagonista**), validación de correo y contraseña.
- **Botón Google Sign-In**: Autenticación en un solo toque mediante Google Credential Manager, eliminando la fricción de contraseñas.
- **Persistencia Transparente**: Reconexión automática al abrir la app si la sesión sigue vigente.

---

### 7.2. Croquis Interactivo de Nicaragua (Canvas 2D con Zoom & Pan)

- **Visualización Cartográfica Soberana**: Mapa dibujado por coordenadas vectoriales que representa la silueta geográfica completa del país y sus dos grandes lagos.
- **Interacción Multitáctil**: Desplazamiento panorámico (*Pan*), zoom fluido (*Pinch*) y botón de recentrado de cámara.
- **Marcadores de Ciudades**: Pins geográficos pulsantes para las 10 Ciudades Creativas. Al tocar un pin se despliega una tarjeta animada con foto de portada y acceso a sus rutas.

---

### 7.3. Ciudades Creativas, Circuitos y Checklist de Puntos de Interes

- **Detalle de Ciudad**: Historia, galería fotográfica y listado de circuitos turísticos organizados por nivel de dificultad (*Baja, Media, Alta*), distancia en kilómetros y duración estimada.
- **Detalle de Circuito**: Ruta paso a paso con hitos culturales, saberes populares, leyendas y gastronomía típica.
- **Checklist GPS de Puntos Visitados**: Al llegar a un sitio histórico o monumento, el turista pulsa "Ya lo visité"; la app consulta el GPS satelital y valida la presencia física con el servidor.

---

### 7.4. Mural de Eventos, Reacciones "Grano de Cafe" y Notificaciones

- **Cartelera Oficial y Mural**: Listado de festivales, ferias gastronómicas y desfiles patronales.
- **Reacción Cultural "Grano de Café"**: Sistema de valoración simbólico que permite a los usuarios respaldar eventos culturales tradicionales de Nicaragua.
- **Confirmación de Asistencia**: Registro de asistencia del usuario con conteo de aforo en tiempo real.
- **Recordatorios Automáticos**: Programación de alarmas mediante el `AlarmManager` con aviso previo a la fecha del evento y botón para trazar la ruta en Google Maps.

---

### 7.5. Feed Social, Carga Masiva de Fotografias y Comentarios

- **Feed Comunitario**: Publicaciones generadas por turistas y artesanos con carrusel fotográfico multi-imagen.
- **Interacciones**: Contador de "Me gusta" y sección de comentarios en tiempo real.
- **Carga Masiva Multipart**: Subida de hasta 10 imágenes simultáneas con optimización de memoria y barra de progreso.

---

### 7.6. Perfil de Usuario y Registro de Emprendimientos (Protagonistas)

- **Edición de Perfil**: Actualización de nombre, teléfono y fotografía de avatar mediante selector multimedia o cámara del dispositivo (`FileProvider`).
- **Módulo de Protagonistas**: Los artesanos y emprendedores locales pueden registrar su negocio (`Empresa`), asociarlo a una Ciudad Creativa y Punto de Interés, definir su ubicación geográfica y habilitar la recepción de inversiones turísticas.
- **Selector de Idioma**: Interfaz accesible para cambiar el idioma global de la app en cualquier momento.

---

### 7.7. Asistente Turistico IA "Eduardo" (Chatbot Georreferenciado)

- **Guía Personalizado**: Chat interactivo con la IA "Eduardo", entrenada con el contexto histórico, turístico y cultural de las Ciudades Creativas.
- **GPS Contextual**: Botón para compartir las coordenadas del usuario con Eduardo, permitiéndole responder preguntas como *"¿Qué lugares históricos me quedan a menos de 500 metros?"* o *"¿Qué plato típico debo probar aquí?"*.
- **Copia Rápida y Limpieza**: Botones para copiar respuestas al portapapeles y reiniciar conversaciones.

---

## 8. Permisos de la Aplicacion (AndroidManifest.xml)

| Permiso | Tipo | Proposito en la Aplicacion |
| :--- | :--- | :--- |
| `android.permission.INTERNET` | Normal | Comunicación HTTP/HTTPS con la API REST y descarga de multimedia. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Detección del estado de la conexión a internet antes de ejecutar peticiones. |
| `android.permission.ACCESS_FINE_LOCATION` | Peligroso (Runtime) | Geoposicionamiento satelital de alta precisión para validación de visitas a Puntos de Interés y contexto de Eduardo. |
| `android.permission.ACCESS_COARSE_LOCATION` | Peligroso (Runtime) | Geoposicionamiento aproximado por red celular o WiFi como respaldo. |
| `android.permission.POST_NOTIFICATIONS` | Peligroso (API 33+) | Emisión de notificaciones del sistema para recordatorios de eventos a los que se asistirá. |
| `android.permission.SCHEDULE_EXACT_ALARM` | Especial (API 31+) | Disparo exacto de alarmas para notificaciones previas al inicio de los eventos culturales. |

---

## 9. Estrategia de Ramas y Control de Versiones

El proyecto sigue una estrategia estricta de 3 ramas descrita en [GUIA_CONTROL_VERSIONES.md](GUIA_CONTROL_VERSIONES.md):

```text
[ feature/<nombre> ] ──► [ beta ] ──► [ master ] (Producción / Release APK)
```

- **`master`**: Rama de producción estable. Cada merge dispara el workflow de compilación y publicación de APK en GitHub Releases.
- **`beta`**: Rama de integración continua y estabilización de pruebas.
- **`feature/<nombre>`**: Ramas temporales de desarrollo para nuevas pantallas o utilidades.
- **Estándar de Mensajes**: Convención **Conventional Commits** en español (`feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `chore:`, `test:`).

---

## 10. Contribucion

1. Realiza un Fork del repositorio.
2. Crea una rama para tu característica:
   ```bash
   git checkout -b feature/nueva-funcionalidad
   ```
3. Registra tus cambios siguiendo la convención de commits:
   ```bash
   git commit -m "feat(ui): agregar animacion en pines de ciudades"
   ```
4. Sube tu rama al repositorio remoto:
   ```bash
   git push origin feature/nueva-funcionalidad
   ```
5. Abre un Pull Request dirigido a la rama `beta`.

---

## 11. Autores y Agradecimientos

- **Ader Zeas** - Arquitectura Android, Desarrollo en Jetpack Compose e Integración con Backend
- **Aurora Loza** - Aseguramiento de Calidad, Diseño de Experiencia (UX) y Pruebas
- **Comisión Nacional de Economía Creativa** y la iniciativa **Hackathon Nicaragua**

---

## 12. Licencia

Este proyecto está bajo la Licencia [MIT](LICENSE).

```text
Copyright (c) 2026 Codice路 (Android)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<div align="center">
  <sub>Desarrollado para el fortalecimiento del turismo cultural y las Ciudades Creativas de Nicaragua</sub>
</div>
