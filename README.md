# 🍹 Forget Shyness - Android App

**Forget Shyness** es una aplicación móvil nativa de Android diseñada para ser el alma de la fiesta. Su objetivo es romper la timidez y fomentar la interacción social a través de dinámicas, juegos y un asistente de planificación de eventos, todo en una interfaz moderna y fluida construida con Jetpack Compose.

---

## ✨ Características Principales

El proyecto se organiza en tres módulos principales de funcionalidades:

### 🎲 Juegos Interactivos
- **Verdad o Reto**: El clásico juego con un toque picante para confesar secretos o cumplir desafíos.
- **Ruleta Picante**: Una ruleta animada que asigna retos aleatorios a los participantes, con un sistema de calificación (like/dislike) para los retos.
- **Gestión de Participantes**: Permite añadir jugadores locales para una partida rápida, asegurando que todos se unan a la diversión.

### 🍸 Asistente de Recetas (Bartender Virtual)
- **Chatbot con IA**: Integración con la API de **Google Gemini** para actuar como un bartender virtual que recomienda recetas de cócteles.
- **Historial de Chats**: Guarda cada conversación en una sala de chat persistente, permitiendo al usuario consultar recetas anteriores.
- **Gestión de Conversaciones**: El usuario puede crear nuevos chats y eliminar los que ya no necesita.

### 🎉 Planificador de Eventos
- **Creación y Edición de Eventos**: Un completo formulario para crear eventos, especificando nombre, descripción, fecha, hora y lista de compras.
- **Integración con Google Maps**: Permite seleccionar la ubicación del evento directamente desde un mapa interactivo, gracias a la **API de Google Maps y Places**.
- **Sistema de Invitaciones**: Busca y selecciona usuarios registrados en la app para enviarles invitaciones.
- **Gestión de Invitaciones**: Los usuarios reciben y pueden aceptar o rechazar invitaciones a eventos, todo sincronizado con Firestore.

---

## ⚙️ Tecnologías y Arquitectura

- **Lenguaje**: 100% **Kotlin**.
- **UI**: **Jetpack Compose** para una interfaz de usuario declarativa, moderna y reactiva.
- **Arquitectura**: Arquitectura limpia y modularizada por funcionalidad, con un patrón **MVVM** adaptable (ViewModel/Repository) y gestión de estado local con `ViewModel` y `remember`.
- **Asincronía**: **Coroutines** y `StateFlow` para operaciones asíncronas y comunicación entre capas.
- **Firebase**:
  - **Authentication**: Validación de usuarios mediante número de teléfono (SMS/OTP).
  - **Cloud Firestore**: Base de datos NoSQL para el registro de usuarios, gestión de eventos, invitaciones y chats.
- **Google Cloud Platform**:
  - **Google Maps Platform**: API de Maps y Places para la selección de ubicaciones.
  - **Google AI**: API de Gemini para potenciar el chatbot de recetas.
- **Navegación**: Sistema de navegación custom basado en estados para controlar el flujo de la UI en Jetpack Compose.

---

## 🚀 Instalación y Configuración

Para clonar y ejecutar este proyecto localmente, sigue estos pasos:

### 1. Prerrequisitos
- **Android Studio**: Versión Iguana o superior.
- **JDK**: Versión 17 o superior.

### 2. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/forget-shyness.git
cd forget-shyness
```

### 3. Configuración de Claves de API

Necesitarás generar tus propias claves de API para los servicios de Google.

1.  **Obtén tus claves de API**:
    - **Google Maps & Places API**: En la [consola de Google Cloud](https://console.cloud.google.com/), habilita las APIs "Maps SDK for Android" y "Places API" y genera una clave de API.
    - **Gemini API**: En la [consola de Google AI Studio](https://aistudio.google.com/app/apikey), crea una nueva clave de API.

2.  **Guarda las claves de forma segura**:
    - Abre (o crea) el archivo `local.properties` en la raíz de tu proyecto.
    - Añade tus claves de la siguiente manera:

      ```properties
      MAPS_API_KEY="TU_CLAVE_DE_API_DE_GOOGLE_MAPS"
      GEMINI_API_KEY="TU_CLAVE_DE_API_DE_GEMINI"
      ```

3.  **Sincroniza tu proyecto**: El archivo `app/build.gradle.kts` ya está configurado para leer estas claves. Solo necesitas sincronizar tu proyecto con los archivos de Gradle (`File > Sync Project with Gradle Files`).

### 4. Solución de Problemas Comunes

#### Problemas con el envío de SMS (OTP)

La autenticación por SMS de Firebase está restringida a dispositivos cuyas "huellas digitales" (claves SHA) están registradas en el proyecto de Firebase.

-   **Si estás en un nuevo entorno de desarrollo** y la verificación por SMS falla, es necesario que un administrador del proyecto registre tu clave **SHA-1 de depuración**. Puedes obtenerla ejecutando el siguiente comando en la terminal de Android Studio y contactando al administrador:
    ```bash
    ./gradlew signingReport
    ```
-   **Como alternativa rápida para pruebas**, puedes iniciar sesión con uno de los siguientes usuarios, cuyo número de teléfono ya está verificado en la base de datos:
    - **Eick Beltrán**: `3134154847`
    - **Juan Pablo**: `3152633558`
    - **Leandro**: `3043865428`

#### El Chatbot de Recetas no responde

Si el Bartender Virtual no responde, es probable que la clave de API de Gemini configurada haya expirado. Para solucionarlo:

1.  Abre el archivo `app/src/main/res/values/strings.xml`.
2.  Busca la clave `generative_api_key`.
3.  Reemplaza el valor actual por esta clave de respaldo:
    ```xml
    <string name="generative_api_key" translatable="false">AIzaSyARx6hktg9JCExBiZ51oORnX_bC0qksrD4</string>
    ```
4.  Vuelve a ejecutar la aplicación.


### 5. Construir y Ejecutar

Una vez completados los pasos anteriores, puedes construir y ejecutar la aplicación en un emulador o en un dispositivo físico directamente desde Android Studio.

---

## 📁 Estructura del Proyecto

El código fuente está organizado en paquetes por funcionalidad para facilitar la navegación y el mantenimiento:

```
com.example.forgetshyness
│
├── data/         # Modelos de datos, Repositorios (Firestore, Chat), y gestores de sesión.
├── events/       # Composables relacionados con la planificación de eventos.
├── games/        # Composables para los juegos (Menú, Participantes, Ruleta, Verdad o Reto).
├── recipes/      # Composables para el módulo de recetas y chatbot.
└── utils/        # Clases de utilidad y constantes.
```
