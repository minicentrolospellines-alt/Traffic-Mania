# Validación de entrega — 6 de octubre de 2026

## Resultado

- **BUILD SUCCESSFUL** con JDK 17, Gradle 8.13 y Android SDK 36.
- `:app:assembleDebug`: APK generado y firma debug verificada con `apksigner verify`.
- `:app:bundleRelease`: AAB de release generado, sin firma de publicación porque no se proporcionó una clave del propietario. No se distribuye ese AAB como archivo publicable.
- `:app:lintDebug`: cero errores; tres avisos sobre orientación fija, adaptación en Android 16 y reglas modernas de extracción de datos. El proyecto solicita orientación vertical; tabletas y ventanas de Android 16 pueden ignorarla. Conviene probar esas pantallas antes de publicar.
- Seis pruebas automatizadas del motor aprobadas. Se verificaron los 500 niveles: únicos, deterministas, sin superposición y con solución.
- Se comprobaron colisiones, mejora de estrellas, tope del nivel 500, guardado validado, premio diario y prevención de monedas infinitas al repetir niveles.
- Prueba de interfaz Chromium aprobada: gesto sobre el tablero, deshacer, pistas, recarga y continuación, victoria, desbloqueo, monedas, premio diario y compras del garaje.
- Interfaz comprobada a 390×844 y 320×568; sin desbordamiento horizontal, controles dentro del área visible y navegación inferior oculta durante la partida.
- Sin errores JavaScript en ese recorrido. Manifest y recursos XML válidos.
- Gradle Wrapper oficial incluido. SHA-256 de la distribución verificado y fijado en sus propiedades.

## Pendiente de verificar en tu entorno

No se ha probado en un teléfono Android físico ni se ha ejecutado la compilación dentro de tu cuenta de GitHub. El APK incluido es una compilación local de prueba, no una publicación de Google Play. Antes de distribuir ampliamente, probar:

1. AdMob y UMP con red, sin red, anuncios no disponibles y consentimiento en las regiones correspondientes.
2. Recompensa solo después de completar el anuncio, cierre del anuncio sin premio y separación del banner.
3. Cierre forzado, actualización conservando datos, distintos tamaños de pantalla y WebView actualizado.
4. AAB firmado con la clave que conservarás y ficha real de Play Console.

No se han conectado IDs reales de anuncios, compras integradas, ranking mundial ni servicios de backend. No se asegura aprobación o rentabilidad.

## Reproducir

```sh
node --test tests/engine.test.cjs
./gradlew :app:assembleDebug :app:bundleRelease :app:lintDebug
```

Las pruebas de interfaz y sus requisitos se explican en el README. `docs/` incluye capturas de esa prueba de navegador.
