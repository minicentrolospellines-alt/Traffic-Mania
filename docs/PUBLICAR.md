# Firma, anuncios y publicación

## 1. Crear y conservar la clave de subida

En tu computador con JDK instalado:

```sh
keytool -genkeypair -v -keystore trafficmania-upload.jks -alias trafficmania -keyalg RSA -keysize 2048 -validity 10000
```

El comando pide las contraseñas; no las escribas dentro del repositorio. Conserva una copia segura del archivo y de las contraseñas. **No crees una clave nueva en cada actualización.**

Convertir a Base64 en PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path .\trafficmania-upload.jks))) | Set-Clipboard
```

En GitHub, abre Settings → Secrets and variables → Actions → New repository secret. Agrega:

| Nombre | Contenido |
| --- | --- |
| `KEYSTORE_BASE64` | Contenido Base64 de tu archivo `.jks` |
| `KEYSTORE_PASSWORD` | Contraseña del archivo |
| `KEY_ALIAS` | `trafficmania`, si usaste el comando anterior |
| `KEY_PASSWORD` | Contraseña de esa clave |

Ejecuta Actions nuevamente y descarga `TrafficMania-AAB-firmado`. No compartas los secretos por chat ni los subas como archivos al repositorio. GitHub decodifica la clave en una carpeta temporal y la elimina al terminar.

Alternativa: Android Studio → Build → Generate Signed App Bundle/APK, elige Android App Bundle y tu clave.

## 2. Configurar AdMob

En tu cuenta registra Traffic Mania con el paquete `cl.negociospyme.trafficmania` y crea tres unidades: banner, intersticial y recompensado. Edita `monetization.properties`:

```properties
ADS_MODE=production
ADMOB_APP_ID=TU_ID_DE_APP_CON_TILDE
BANNER_ID=TU_UNIDAD_BANNER
INTERSTITIAL_ID=TU_UNIDAD_INTERSTICIAL
REWARDED_ID=TU_UNIDAD_RECOMPENSADA
```

El ID de aplicación contiene `~`. Los IDs de unidades contienen `/`. Las cadenas de ejemplo de este bloque no son IDs válidos: debes reemplazarlas. El proyecto rechaza IDs de producción incompletos al compilar.

Configura Privacy & messaging en AdMob y comprueba UMP en las regiones correspondientes. Configura el sitio de desarrollador y `app-ads.txt` con los datos exactos de tu cuenta cuando corresponda. No se incluye un ID de editor inventado.

El modo debug usa anuncios de prueba incluso cuando el archivo está en producción. Prueba recompensas sin tocar anuncios reales. Revisa disponibilidad y consentimiento en un teléfono real. No se cargan anuncios si UMP no autoriza solicitudes.

## 3. Preparar Play Console

- Usa el AAB firmado de release. Conserva el paquete y la clave para las actualizaciones.
- Completa ficha, icono de 512×512, gráfico destacado y capturas reales del juego en tu teléfono. Se incluye `store/icon-512.png` para la ficha; las capturas de `docs/` son vistas de navegador para revisar el diseño. Captura la versión Android definitiva antes de enviarla a la tienda.
- Publica una política de privacidad con tu contacto real. `PRIVACIDAD_BORRADOR.md` es una base para revisar y completar.
- Declara que contiene anuncios. Revisa Seguridad de los datos según los SDK que efectivamente uses y su configuración. El guardado local del juego no elimina los tratamientos de datos de los SDK publicitarios.
- Define audiencia y clasificación según tu producto real. Esta versión no configura un tratamiento infantil específico; si vas a dirigirla a niños, revisa primero diseño y configuración de los SDK y las exigencias aplicables.
- Prueba cierre/reapertura, red lenta, modo avión, anuncios, consentimiento, botón Atrás, pantallas pequeñas y actualización sin perder progreso.
- Sigue los requisitos de prueba y acceso a producción que muestre tu cuenta de Play Console; no se puede completar ese proceso desde un ZIP.

## Actualizaciones

Incrementa `versionCode` (2, 3...) y ajusta `versionName`. Mantén `applicationId`, firma y clave local de guardado. No cambies las semillas del generador para los niveles existentes sin migrar las partidas guardadas.

## Fuentes técnicas

Documentación consultada el 6 de octubre de 2026:
- https://developers.google.com/admob/android/quick-start
- https://developers.google.com/admob/android/privacy
- https://developer.android.com/build/releases/agp-8-13-0-release-notes

Estas instrucciones no sustituyen la validación de tu configuración concreta en las consolas.
