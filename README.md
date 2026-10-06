# 🚗 Traffic Mania — Parking Jam

Juego Android offline en español, creado para NegociosPyme. Proyecto completo de código fuente para subir a GitHub y compilar. Paquete de producción: `cl.negociospyme.trafficmania`.

**Empieza por [LEEME_PRIMERO.md](LEEME_PRIMERO.md).**

## Incluido

- 500 estacionamientos deterministas, únicos y con solución verificada automáticamente.
- Salida tocando o deslizando en la dirección de la flecha; detección de colisiones y animaciones.
- Autos, motos, buses, camiones y ambulancias. Árboles y barreras fijas.
- Seis ambientes de color: ciudad, supermercado, playa, aeropuerto, mall y noche.
- Progreso local, continuar una partida interrumpida, 1–3 estrellas, mejores tiempos y choques.
- Monedas, seis colecciones de pintura, compra y selección de estilos.
- Premio diario de siete días, racha de niveles perfectos y récords personales.
- Tres pistas iniciales; después monedas o anuncio recompensado. Reintentar y deshacer gratis.
- Pausa, confirmación para salir, botón Atrás Android, sonido, vibración y movimiento reducido.
- Juego y recursos empaquetados, sin CDN, fuentes externas ni conexión necesaria para jugar.
- Android WebView local aislada con AndroidX WebViewAssetLoader; anuncios en vistas nativas.
- AdMob banner adaptativo separado de los controles, intersticial cada cuatro niveles nuevos y nunca durante un movimiento, separación mínima de 90 segundos entre pantallas publicitarias. Anuncios recompensados para pista o duplicar el premio del nivel.
- UMP para solicitud de consentimiento y opciones de privacidad. IDs oficiales de prueba por defecto.
- GitHub Actions para probar niveles, hacer lint y generar APK/AAB.

## Alcance real de esta versión

La vista del juego es cenital 2D con volumen y sombras, no un mundo 3D. Cada vehículo sale en la dirección de su flecha si su recorrido completo está libre; no hay movimiento parcial ni marcha atrás. Las barreras son fijas. Los 500 niveles se generan a partir de semillas, no son 500 mapas dibujados manualmente. Los escenarios comparten el tablero y cambian ambientación y color. El garaje desbloquea pinturas para toda la flota; los tipos de vehículos aparecen con la dificultad.

El ranking es **personal y local**. No hay servidor, multijugador, sincronización entre teléfonos, cuentas, notificaciones ni analítica. La compra «quitar anuncios» es una ampliación futura: no está implementada ni se muestra un botón de compra ficticio. No se han configurado tus unidades reales de AdMob, una ficha Play Console o tus claves de firma. No se garantiza monetización, popularidad ni aprobación en Google Play.

## Herramientas

JDK 17 · Gradle 8.13 · Android Gradle Plugin 8.13.0 · SDK 36 · minSdk 24 (Android 7+) · Android WebView actualizado.

```sh
node --test tests/engine.test.cjs
./gradlew :app:assembleDebug :app:bundleRelease :app:lintDebug
```

En Windows: `gradlew.bat :app:assembleDebug`. Android Studio puede abrir esta carpeta directamente. La primera compilación descarga Gradle, SDK/dependencias y necesita Internet.

Vista de escritorio para verificar el juego sin anuncios nativos:

```sh
python -m http.server 8080 --directory app/src/main/assets/game
```

Abre `http://localhost:8080`. El progreso de esta vista es independiente del de Android.

## Estructura

- `app/src/main/assets/game/`: interfaz, motor, dibujos y guardado.
- `app/src/main/java/.../MainActivity.java`: Android, AdMob, privacidad y navegación.
- `monetization.properties`: modo de anuncios e identificadores.
- `.github/workflows/android.yml`: compilación en GitHub.
- `tests/engine.test.cjs`: niveles, colisiones, economía y guardado.
- `docs/`: publicación, privacidad y validación.

## Datos y recompensas

El progreso usa `localStorage` de la WebView. Desinstalar la app o borrar sus datos elimina el progreso. No hay copia en nube. Los primeros 100 créditos y tres pistas permiten jugar inmediatamente. Una victoria nueva entrega 25–35 monedas; repetir solo entrega la diferencia si mejora la cantidad de estrellas. Una recompensa publicitaria requiere el callback real del SDK. Sin anuncio, no se entrega premio ni se bloquea el juego. El premio diario usa la fecha local; no es un sistema antifraude de servidor.

Para futuras actualizaciones conserva el `applicationId`, la clave de firma y el nombre del guardado. Sube `versionCode` y `versionName` en `app/build.gradle`.

Hecho por NegociosPyme by Juan Alarcón.

## Capturas y pruebas de interfaz

Ver [las vistas del juego](docs/VISTAS.md). Se incluye icono de ficha en `store/icon-512.png`.

Prueba de interfaz opcional, además de las pruebas del motor sin dependencias:

```sh
npm install --no-save playwright
npx playwright install chromium
node tests/ui.test.cjs
```

Abre un servidor temporal local, ejecuta gestos y recorridos de interfaz, genera capturas en `docs/` y lo cierra al terminar. `TRAFFIC_BROWSER` permite indicar un ejecutable de Chromium ya instalado. No forma parte de las dependencias de la aplicación Android.
