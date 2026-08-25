package co.japl.android.ev_ride_connect.ble

/**
 * Configuration data holder for Tuya Smart / Ride BLE SDK integration.
 *
 * @param appKey The AppKey registered in the Tuya Developer Console (iot.tuya.com).
 * @param appSecret The AppSecret registered in the Tuya Developer Console.
 * @param deviceUuid Optional target Tuya Device UUID.
 * @param authKey Optional Tuya device authorization key for encrypted BLE handshake.
 * @param isProduction Specifies whether the SDK operates in production mode.
 */
data class TuyaSdkConfig(
    val appKey: String = "",
    val appSecret: String = "",
    val deviceUuid: String? = null,
    val authKey: String? = null,
    val isProduction: Boolean = true
) {
    /**
     * Returns true if valid Tuya SDK credentials (AppKey and AppSecret) are present.
     */
    fun isConfigured(): Boolean {
        return appKey.isNotBlank() && appSecret.isNotBlank()
    }
}
