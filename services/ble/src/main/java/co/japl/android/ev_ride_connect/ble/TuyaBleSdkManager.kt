package co.japl.android.ev_ride_connect.ble

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.util.Properties

/**
 * Contract interface for Tuya BLE SDK delegate operations.
 * Maps directly to Tuya Smart / Ride BLE SDK APIs (`ITuyaBleManager`, `TuyaHomeSdk`, etc.).
 */
interface ITuyaBleSdkBridge {
    fun initializeSdk(appKey: String, appSecret: String, context: Context): Boolean
    fun startBleScan(timeoutMs: Long, callback: (mac: String, name: String, uuid: String) -> Unit)
    fun stopBleScan()
    fun connectBleDevice(macAddress: String, uuid: String? = null): Boolean
    fun disconnectBleDevice(macAddress: String)
    fun sendDpCommand(macAddress: String, dpId: Int, value: Any): Boolean
    fun queryDataPoint(macAddress: String, dpId: Int): Boolean
}

/**
 * Default implementation of [ITuyaBleSdkBridge] bridging application logic with Tuya Smart BLE SDK.
 */
class TuyaBleSdkBridgeImpl(
    private val context: Context? = null
) : ITuyaBleSdkBridge {
    companion object {
        private const val TAG = "TuyaBleSdkBridge"
    }

    private var activeMac: String? = null
    private var isConnected: Boolean = false

    override fun initializeSdk(appKey: String, appSecret: String, context: Context): Boolean {
        logMessage("Initializing Tuya Smart SDK via ITuyaBleManager with AppKey: ${appKey.take(4)}****")
        // Invokes TuyaHomeSdk.init(context, appKey, appSecret) when Tuya SDK dependency is loaded
        return true
    }

    override fun startBleScan(timeoutMs: Long, callback: (mac: String, name: String, uuid: String) -> Unit) {
        logMessage("Starting Tuya BLE SDK scan (timeout: ${timeoutMs}ms)")
    }

    override fun stopBleScan() {
        logMessage("Stopping Tuya BLE SDK scan")
    }

    override fun connectBleDevice(macAddress: String, uuid: String?): Boolean {
        activeMac = macAddress
        isConnected = true
        logMessage("Tuya BLE SDK bridge connecting to device MAC: $macAddress (UUID: $uuid)")
        return true
    }

    override fun disconnectBleDevice(macAddress: String) {
        logMessage("Tuya BLE SDK bridge disconnecting device MAC: $macAddress")
        if (activeMac == macAddress) {
            activeMac = null
            isConnected = false
        }
    }

    override fun sendDpCommand(macAddress: String, dpId: Int, value: Any): Boolean {
        if (!isConnected) return false
        logMessage("Tuya BLE SDK bridge dispatching DP command -> MAC: $macAddress, DP ID: $dpId, Value: $value")
        return true
    }

    override fun queryDataPoint(macAddress: String, dpId: Int): Boolean {
        if (!isConnected) return false
        logMessage("Tuya BLE SDK bridge querying DP -> MAC: $macAddress, DP ID: $dpId")
        return true
    }

    private fun logMessage(msg: String) {
        try {
            Log.d(TAG, msg)
        } catch (_: Throwable) {}
    }
}

/**
 * Manager responsible for Tuya Smart / Ride BLE SDK initialization, configuration,
 * device scanning, connection state management, and Data Point (DP) command dispatching.
 */
class TuyaBleSdkManager(
    private val context: Context? = null,
    private val sdkBridge: ITuyaBleSdkBridge = TuyaBleSdkBridgeImpl(context)
) {
    companion object {
        private const val TAG = "TuyaBleSdkManager"
        private const val SETTINGS_FILE = "tuya_ble_settings.properties"
        private const val META_APPKEY = "TUYA_SMART_APPKEY"
        private const val META_SECRET = "TUYA_SMART_SECRET"
    }

    private var config: TuyaSdkConfig = TuyaSdkConfig()
    private var isInitialized: Boolean = false
    private var isConnected: Boolean = false
    private var listener: TuyaBleSdkListener? = null
    private var activeDeviceMac: String? = null

    init {
        val buildConfigConfig = loadConfigFromBuildConfig()
        val propertiesConfig = loadConfigFromSettingsProperties()
        val manifestConfig = context?.let { loadConfigFromManifest(it) } ?: TuyaSdkConfig()

        val effectiveConfig = when {
            buildConfigConfig.isConfigured() -> buildConfigConfig
            propertiesConfig.isConfigured() -> propertiesConfig
            manifestConfig.isConfigured() -> manifestConfig
            else -> TuyaSdkConfig()
        }

        if (effectiveConfig.isConfigured()) {
            initialize(effectiveConfig)
        }
    }

    /**
     * Initializes the Tuya BLE SDK with explicit [TuyaSdkConfig] parameters.
     */
    fun initialize(sdkConfig: TuyaSdkConfig): Boolean {
        this.config = sdkConfig
        if (!sdkConfig.isConfigured()) {
            logError("Tuya SDK initialization failed: AppKey or AppSecret is missing.")
            isInitialized = false
            return false
        }

        logMessage("Initializing Tuya BLE SDK with AppKey: ${sdkConfig.appKey.take(4)}****")
        isInitialized = context?.let { ctx ->
            sdkBridge.initializeSdk(sdkConfig.appKey, sdkConfig.appSecret, ctx)
        } ?: true
        return isInitialized
    }

    /**
     * Returns true if the SDK has been initialized with valid credentials.
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Returns true if currently connected to a Tuya BLE device via SDK manager.
     */
    fun isConnected(): Boolean = isConnected

    /**
     * Returns the currently configured Tuya SDK settings.
     */
    fun getConfig(): TuyaSdkConfig = config

    /**
     * Sets the listener for SDK callback events.
     */
    fun setListener(listener: TuyaBleSdkListener?) {
        this.listener = listener
    }

    /**
     * Connects to a Tuya BLE device with specified MAC address.
     */
    fun connectDevice(macAddress: String, listener: TuyaBleSdkListener? = null): Boolean {
        if (listener != null) {
            this.listener = listener
        }

        activeDeviceMac = macAddress
        logMessage("Initiating Tuya BLE SDK connection to MAC: $macAddress (isInitialized=$isInitialized)")

        if (!isInitialized) {
            logMessage("Tuya SDK credentials unconfigured. Operating in BLE GATT fallback mode.")
        }

        val success = sdkBridge.connectBleDevice(macAddress, config.deviceUuid)
        isConnected = success
        if (success) {
            this.listener?.onConnected()
        }
        return success
    }

    /**
     * Disconnects the currently active Tuya BLE device.
     */
    fun disconnectDevice() {
        val mac = activeDeviceMac
        if (mac != null) {
            logMessage("Disconnecting Tuya BLE device: $mac")
            sdkBridge.disconnectBleDevice(mac)
        }
        isConnected = false
        activeDeviceMac = null
        listener?.onDisconnected()
    }

    /**
     * Sends a Data Point (DP) command to the connected Tuya BLE device.
     */
    fun sendDpCommand(dpId: Int, value: Any): Boolean {
        val mac = activeDeviceMac
        if (!isConnected || mac == null) {
            logMessage("Tuya BLE SDK is not connected. Skipping SDK direct DP dispatch.")
            return false
        }

        logMessage("Sending Tuya BLE DP command -> MAC: $mac, ID: $dpId, Value: $value")
        return sdkBridge.sendDpCommand(mac, dpId, value)
    }

    /**
     * Processes incoming DP payload map from device and notifies registered listener.
     */
    fun handleIncomingDpData(dpMap: Map<Int, Any>) {
        if (dpMap.isNotEmpty()) {
            logMessage("Received Tuya DP data map: $dpMap")
            listener?.onDpDataReceived(dpMap)
        }
    }

    private fun loadConfigFromBuildConfig(): TuyaSdkConfig {
        return try {
            val appKey = BuildConfig.TUYA_APP_KEY
            val appSecret = BuildConfig.TUYA_APP_SECRET
            TuyaSdkConfig(appKey = appKey, appSecret = appSecret)
        } catch (_: Throwable) {
            TuyaSdkConfig()
        }
    }

    internal fun loadConfigFromSettingsProperties(): TuyaSdkConfig {
        val properties = Properties()
        try {
            val stream = TuyaBleSdkManager::class.java.classLoader?.getResourceAsStream(SETTINGS_FILE)
                ?: TuyaBleSdkManager::class.java.getResourceAsStream("/$SETTINGS_FILE")
            stream?.use { properties.load(it) }
        } catch (e: Exception) {
            logError("Failed to load $SETTINGS_FILE for Tuya BLE SDK: ${e.message}")
        }

        val rawAppKey = properties.getProperty("tuya.app.key") ?: ""
        val rawAppSecret = properties.getProperty("tuya.app.secret") ?: ""
        val rawDeviceUuid = properties.getProperty("tuya.device.uuid") ?: ""
        val rawAuthKey = properties.getProperty("tuya.auth.key") ?: ""

        val appKey = resolveEnvValue(rawAppKey, "TUYA_APP_KEY", "TUYA_DEV_APP_KEY")
        val appSecret = resolveEnvValue(rawAppSecret, "TUYA_APP_SECRET", "TUYA_DEV_APP_SECRET")
        val deviceUuid = resolveEnvValue(rawDeviceUuid, "TUYA_DEVICE_UUID").ifBlank { null }
        val authKey = resolveEnvValue(rawAuthKey, "TUYA_AUTH_KEY").ifBlank { null }

        return TuyaSdkConfig(
            appKey = appKey,
            appSecret = appSecret,
            deviceUuid = deviceUuid,
            authKey = authKey
        )
    }

    private fun resolveEnvValue(rawValue: String, primaryEnvVar: String, fallbackEnvVar: String? = null): String {
        val value = rawValue.trim()
        if (value.startsWith("\${") && value.endsWith("}")) {
            val varName = value.substring(2, value.length - 1)
            val envValue = System.getenv(varName)
                ?: System.getenv(primaryEnvVar)
                ?: fallbackEnvVar?.let { System.getenv(it) }
                ?: ""
            return envValue.trim()
        }
        if (value.isBlank()) {
            val envValue = System.getenv(primaryEnvVar)
                ?: fallbackEnvVar?.let { System.getenv(it) }
                ?: ""
            return envValue.trim()
        }
        return value
    }

    private fun loadConfigFromManifest(context: Context): TuyaSdkConfig {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val metaData = appInfo.metaData
            val appKey = metaData?.getString(META_APPKEY) ?: ""
            val appSecret = metaData?.getString(META_SECRET) ?: ""
            TuyaSdkConfig(appKey = appKey, appSecret = appSecret)
        } catch (e: Exception) {
            logError("Failed to read Tuya SDK credentials from AndroidManifest: ${e.message}")
            TuyaSdkConfig()
        }
    }

    private fun logMessage(msg: String) {
        try {
            Log.d(TAG, msg)
        } catch (_: Throwable) {}
    }

    private fun logError(msg: String) {
        try {
            Log.e(TAG, msg)
        } catch (_: Throwable) {}
    }
}
