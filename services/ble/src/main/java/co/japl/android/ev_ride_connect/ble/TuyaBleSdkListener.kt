package co.japl.android.ev_ride_connect.ble

/**
 * Interface listener for Tuya BLE SDK lifecycle events and Data Point (DP) updates.
 */
interface TuyaBleSdkListener {
    fun onConnected()
    fun onDisconnected()
    fun onError(errorCode: Int, errorMessage: String)
    fun onDpDataReceived(dpMap: Map<Int, Any>)
}
