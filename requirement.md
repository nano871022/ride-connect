# Requerimientos y Guía de Configuración para el SDK de Tuya Ride / Smart BLE

## 1. Estado Actual del Repositorio

Se ha completado la arquitectura e integración del SDK de Tuya BLE dentro del módulo `:services:ble` mediante la arquitectura limpia del proyecto:

1. **Manager del SDK (`TuyaBleSdkManager`)**: Administra la inicialización con credenciales, estado de conexión, listeners de eventos y despacho de comandos DP (Data Points).
2. **Configuración (`TuyaSdkConfig`)**: Contiene las credenciales del desarrollador (`appKey`, `appSecret`, `deviceUuid`, `authKey`). Carga automáticamente las credenciales desde los metadatos de `AndroidManifest.xml` o mediante inicialización programática.
3. **Adaptador BLE (`TuyaBleAdapter`)**: Integre `TuyaBleSdkManager` manteniendo el contrato de la interfaz `BleScooterPort`, el cálculo de porcentaje de batería Li-ion (13S), registro de logs (`BleLogEntry`), manejo de reintentos y compatibilidad BLE GATT.
4. **Repositorios Gradle**: Se configuró la URL del repositorio oficial Maven de Tuya en `settings.gradle.kts` (`https://maven-other.tuya.com/repository/maven-releases/`) y las entradas de biblioteca en `gradle/libs.versions.toml`.

---

## 2. Requisitos Previos en Tuya IoT Platform

Para poder conectarse exitosamente a la scooter eléctrica mediante la infraestructura oficial de Tuya, debes obtener las credenciales de la plataforma de desarrolladores de Tuya:

### Paso 1: Registro en Tuya IoT Platform
1. Ingresa a [Tuya IoT Platform](https://iot.tuya.com/) y crea o inicia sesión en tu cuenta de desarrollador.
2. Ve a **App Service** -> **App SDK** -> **Development**.

### Paso 2: Crear / Registrar la Aplicación Móvil
1. Crea una nueva App seleccionando **Smart Life App SDK** o **Tuya Smart SDK**.
2. Ingresa los siguientes datos de la aplicación:
   - **App Name**: EVRideConnect
   - **Package Name**: `co.japl.android.ev_ride_connect`
   - **SHA-256 Signature**: Obtén la huella SHA-256 de tu clave de firma (Keystore Debug/Release).
     - *Comando para Keystore de desarrollo*:
       ```bash
       keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
       ```
3. Una vez guardado, Tuya generará las credenciales:
   - **AppKey** (ejemplo: `a1b2c3d4e5f6g7h8`)
   - **AppSecret** (ejemplo: `z9y8x7w6v5u4t3s2`)

---

## 3. Configuración de Credenciales en el Repositorio

El proyecto soporta dos formas para inyectar tus credenciales:

### Opción A: A través de `AndroidManifest.xml` (Recomendado)
Añade las siguientes etiquetas `<meta-data>` dentro del bloque `<application>` en `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/resources/android">
    <application ...>

        <!-- Credenciales del SDK de Tuya Smart / Ride -->
        <meta-data
            android:name="TUYA_SMART_APPKEY"
            android:value="TU_APP_KEY_AQUI" />
        <meta-data
            android:name="TUYA_SMART_SECRET"
            android:value="TU_APP_SECRET_AQUI" />

    </application>
</manifest>
```

### Opción B: Inicialización Programática
Puedes inicializar el SDK directamente en `EvRideConnectApp.kt` o mediante inyección de dependencias Hilt:

```kotlin
@Inject
lateinit var tuyaSdkManager: TuyaBleSdkManager

override fun onCreate() {
    super.onCreate()
    tuyaSdkManager.initialize(
        TuyaSdkConfig(
            appKey = "TU_APP_KEY_AQUI",
            appSecret = "TU_APP_SECRET_AQUI",
            isProduction = true
        )
    )
}
```

---

## 4. Mapeo de Puntos de Datos (DP - Data Points) de la Scooter

La scooter VSETT / Tuya Ride transmite y recibe telemetría mediante los siguientes Data Points (DPs):

| DP ID | Tipo | Descripción | Mapeo en `ScooterState` |
|---|---|---|---|
| **1** | `Boolean` | Bloqueo / Desbloqueo electrónico | `isLocked` (`true` = Bloqueado, `false` = Desbloqueado) |
| **2** | `Enum` / `Int` | Modo / Marcha de velocidad | `speedMode` (1, 2, 3) |
| **4** | `Boolean` | Encendido de faros / luces | `isLightOn` (`true` = On, `false` = Off) |
| **5** | `Int` | Velocidad actual en tiempo real | `currentSpeed` (km/h) |
| **6** | `Int` | Odómetro total acumulado | `totalOdometer` (km) |
| **7** | `Int` | Voltaje de batería en tiempo real | `realtimeVoltage` (décimas de Voltio, ej. `546` = 54.6V). Mapeado automáticamente a porcentaje de batería Li-ion 13S con `BatteryCalculator`. |

---

## 5. Instrucciones para Ejecutar y Finalizar la Configuración

1. **Ingresar Credenciales**: Coloca tu `AppKey` y `AppSecret` reales en `AndroidManifest.xml` (o vía `TuyaSdkConfig`).
2. **Sincronizar Proyecto**: Sincroniza Gradle con la configuración agregada.
3. **Compilar y Ejecutar**:
   ```bash
   ./gradlew assembleDebug
   ```
4. **Verificación en Dispositivo Real**:
   - Conecta el dispositivo Android vía USB o ADB.
   - Enciende el Bluetooth y habilita los permisos de ubicación (`ACCESS_FINE_LOCATION`) y Bluetooth (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`).
   - Abre la aplicación **EVRideConnect**, selecciona la opción de conectar patinete y valida que el estado de conexión cambie a `Conectado` actualizando los valores de telemetría en tiempo real.
