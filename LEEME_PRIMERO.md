# Primero: subirlo a GitHub

1. Descomprime este ZIP en tu computador.
2. Crea un repositorio nuevo llamado **TrafficMania** en GitHub.
3. Sube **el contenido de la carpeta TrafficMania**, no el ZIP. En la raíz del repositorio deben quedar `settings.gradle`, `app`, `gradle` y `.github`.
4. Revisa que también se haya subido `.github/workflows/android.yml`. Esa carpeta puede estar oculta en algunos exploradores.
5. Entra en **Actions → Traffic Mania - APK y AAB**. La compilación se ejecuta al subir a `main` o `master`. También puedes usar **Run workflow**.
6. Espera a que termine. Si sale verde, al final entra en **Artifacts → TrafficMania-APK-prueba**.
7. Descarga, descomprime e instala el APK en tu Android.

**El archivo `TrafficMania-AAB-SIN-FIRMA` no sirve para subir a Play Console.** El flujo genera un AAB firmado únicamente después de configurar tus cuatro secretos de firma. Lee `docs/PUBLICAR.md`.

El APK de prueba tiene paquete `cl.negociospyme.trafficmania.debug` y puede convivir con la versión de producción. Si GitHub genera otra clave debug y Android rechaza una actualización del APK de prueba, desinstala la prueba anterior (se pierde su progreso) o configura firma persistente antes de repartir pruebas repetidas. Las versiones finales deben usar siempre la misma clave.

## Qué probar primero

- Nivel 1: toca un auto con camino libre; después intenta uno bloqueado.
- Completa el nivel y revisa monedas, estrellas y acceso al siguiente.
- Vuelve al inicio desde una partida; pulsa continuar.
- Cierra y abre la app, prueba el modo avión.
- Usa una pista, deshaz una salida y reintenta.
- Recoge el premio diario y comprueba que no se puede recoger dos veces.
- Abre el garaje y los ajustes.
- Con Internet prueba anuncios identificados como anuncios de prueba.

## Anuncios

El proyecto viene en modo **test**. No genera ingresos. No cambies a producción hasta tener tus cuatro identificadores reales de AdMob y haber configurado los mensajes de privacidad. Los anuncios de prueba también pueden tardar o no estar disponibles.

## Si Actions falla

Abre la ejecución roja, entra en el paso rojo y copia el error o envía una captura. El informe `docs/VALIDACION.md` distingue las comprobaciones realizadas de las que aún requieren compilar y probar Android.
