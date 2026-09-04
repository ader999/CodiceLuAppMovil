# Guía de Control de Versiones y Flujo de Trabajo Profesional (Git)

Esta guía define el flujo de trabajo profesional para el proyecto **Codise**, cumpliendo con los estándares de ramas organizadas, **Conventional Commits en español**, gestión de **Pull Requests (PR)**, **trazabilidad** y **convergencia entre ramas**.

---

## 1. Estructura y Estrategia de Ramas (Mínimo 3 Ramas)

Para garantizar estabilidad y orden, el repositorio se estructura en tres niveles de ramas:

```
[ feature/autenticacion-google ] ──┐ (Desarrollo puntual)
                                   ▼
[ beta ] (o develop) ──────────────┴───► Pruebas e integración continua
                                   │
                                   ▼
[ master ] (o main) ───────────────┴───► Versión estable / Producción
```

### 1.1. Rama Principal (`master` / `main`)
- **Propósito:** Contiene el código fuente en estado **completamente estable**, probado y listo para entrega o producción.
- **Regla de oro:** Nunca se hace `commit` o `push` directo a `master`. Todo cambio debe llegar a través de un Pull Request desde `beta`.

### 1.2. Rama de Integración (`beta` / `develop`)
- **Propósito:** Rama central donde convergen las nuevas funciones terminadas antes de pasar a producción.
- **Uso:** Sirve para realizar pruebas de integración, validación de diseño y pruebas generales de la aplicación.
- **Origen:** Se crea a partir de `master`.

### 1.3. Ramas de Funcionalidad (`feature/<nombre-en-espanol>`)
- **Propósito:** Cada nueva opción, pantalla o requerimiento se desarrolla en una rama aislada e independiente.
- **Nomenclatura:** `feature/<funcionalidad>` (ejemplos: `feature/autenticacion-google`, `feature/pantalla-perfil`, `feature/recuperacion-contrasena`).
- **Origen:** Se crean a partir de `beta` (o se sincronizan con ella) y una vez listas se integran a `beta` mediante Pull Request.

*(Opcional para errores urgentes)*: `hotfix/<nombre>` para parches críticos directamente sobre `master`.

---

## 2. Convención de Commits (Conventional Commits en Español)

Todos los mensajes de confirmación (`commits`) deben escribirse en **español** y seguir el estándar internacional de **Conventional Commits**:

### Formato general:
```text
<tipo>(<ámbito opcional>): <descripción breve en imperativo o presente>

[cuerpo opcional con más detalle]

[pie opcional: referencias a tareas, incidencias o breaking changes]
```

### Tipos permitidos:
| Tipo | Cuándo usarlo | Ejemplo en Español |
| :--- | :--- | :--- |
| **`feat`** | Una nueva funcionalidad o pantalla | `feat(auth): agregar inicio de sesión con Google` |
| **`fix`** | Corrección de un error o bug | `fix(login): corregir cierre inesperado al presionar botón` |
| **`docs`** | Modificaciones en documentación | `docs: agregar guía de flujo de trabajo git y ramas` |
| **`style`** | Ajustes visuales, espaciados o formato sin alterar lógica | `style(ui): ajustar padding y colores en pantalla de login` |
| **`refactor`** | Reestructuración de código sin cambiar comportamiento | `refactor(api): reorganizar llamadas a servicio de autenticación` |
| **`chore`** | Actualización de dependencias, Gradle, configuraciones | `chore(deps): agregar dependencia de credenciales de Google` |
| **`test`** | Creación o actualización de pruebas unitarias/instrumentadas | `test(auth): agregar pruebas para validación de correo` |

### Buenas prácticas de mensajes:
1. Usar verbos en infinitivo o presente: `agregar`, `corregir`, `actualizar` (evitar mensajes ambiguos como *"cambios"* o *"arreglos"*).
2. La primera línea no debe exceder 72 caracteres.
3. Todo el texto en **español**.

---

## 3. Ciclo de Trabajo Paso a Paso (Workflow)

A continuación se muestra el ciclo completo para desarrollar una nueva función:

### Paso 1: Asegurarse de tener `beta` actualizada
```bash
git checkout beta
git pull origin beta
```

### Paso 2: Crear la rama para la nueva funcionalidad
```bash
git checkout -b feature/autenticacion-google
```

### Paso 3: Realizar cambios y registrar commits con Conventional Commits
```bash
git add app/src/main/java/com/example/codise/utils/AutenticadorGoogle.kt
git commit -m "feat(auth): implementar helper de autenticador de Google"

git add app/build.gradle.kts gradle/libs.versions.toml
git commit -m "chore(deps): agregar dependencias de Credential Manager"
```

### Paso 4: Subir la rama al repositorio remoto (GitHub / GitLab)
```bash
git push -u origin feature/autenticacion-google
```

---

## 4. Pull Requests (PR) y Trazabilidad

Para garantizar la trazabilidad y revisión del código:

1. **Creación del PR:**
   - En la plataforma web (GitHub/GitLab), crear un Pull Request desde `feature/autenticacion-google` con destino a `beta`.
2. **Plantilla de descripción sugerida para el PR:**
   ```markdown
   ## Resumen de cambios
   - Se integró el flujo de autenticación con Google usando Credential Manager.
   - Se actualizó la interfaz de PantallaInicioSesion para incluir el botón de Google.

   ## Tipo de cambio
   - [x] Nueva característica (feat)
   - [ ] Corrección de error (fix)
   - [ ] Tarea de configuración (chore)

   ## Pruebas realizadas
   - Probado en emulador Pixel 7 con cuenta de prueba de Google.
   - Verificado que las credenciales se procesen correctamente en el ViewModel.
   ```
3. **Revisión y Convergencia:**
   - Se revisa el código (code review).
   - Una vez aprobado, se realiza la convergencia (Merge) hacia `beta`.

---

## 5. Convergencia y Promoción a Producción (`beta` ➔ `master`)

Cuando un conjunto de características en `beta` ha sido probado y está listo para entrega:

1. Crear un Pull Request desde `beta` hacia `master`.
   - **Título sugerido:** `release: versión 1.1.0 con autenticación de Google y mejoras`
2. Al realizar el Merge en `master`, se puede crear una etiqueta de versión (Tag):
   ```bash
   git checkout master
   git pull origin master
   git tag -a v1.1.0 -m "Versión 1.1.0: Autenticación con Google integrada"
   git push origin v1.1.0
   ```
3. Si durante el proceso hubo cambios en `master`, sincronizar `beta` con `master` para mantener la convergencia:
   ```bash
   git checkout beta
   git merge master
   git push origin beta
   ```

---

## 6. Resumen de Comandos Frecuentes

| Acción | Comando |
| :--- | :--- |
| Ver estado y rama actual | `git status` |
| Listar ramas locales y remotas | `git branch -a` |
| Crear y cambiarse a rama `beta` | `git checkout -b beta` |
| Crear y cambiarse a una feature | `git checkout -b feature/nombre-funcion` |
| Cambiar a una rama existente | `git checkout nombre-rama` |
| Guardar cambios con mensaje estándar | `git commit -m "feat(modulo): descripcion"` |
| Subir rama por primera vez | `git push -u origin nombre-rama` |
| Actualizar rama local con remoto | `git pull origin nombre-rama` |
