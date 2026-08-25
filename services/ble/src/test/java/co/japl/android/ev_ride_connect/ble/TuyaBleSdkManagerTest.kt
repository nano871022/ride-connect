package co.japl.android.ev_ride_connect.ble

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TuyaBleSdkManagerTest {

    private lateinit var sdkManager: TuyaBleSdkManager

    @Before
    fun setUp() {
        sdkManager = TuyaBleSdkManager()
    }

    @Test
    fun shouldReturnUninitializedByDefaultWhenNoCredentialsProvided() {
        assertThat(sdkManager.isInitialized()).isFalse()
        assertThat(sdkManager.getConfig().isConfigured()).isFalse()
    }

    @Test
    fun shouldInitializeSuccessfullyWhenValidCredentialsProvided() {
        val config = TuyaSdkConfig(
            appKey = "testAppKey123",
            appSecret = "testAppSecret456"
        )

        val result = sdkManager.initialize(config)

        assertThat(result).isTrue()
        assertThat(sdkManager.isInitialized()).isTrue()
        assertThat(sdkManager.getConfig().appKey).isEqualTo("testAppKey123")
    }

    @Test
    fun shouldFailInitializationWhenCredentialsAreEmpty() {
        val emptyConfig = TuyaSdkConfig(appKey = "", appSecret = "")

        val result = sdkManager.initialize(emptyConfig)

        assertThat(result).isFalse()
        assertThat(sdkManager.isInitialized()).isFalse()
    }

    @Test
    fun shouldUpdateConnectionStateAndNotifyListenerOnConnect() {
        var connectedCalled = false
        val listener = object : TuyaBleSdkListener {
            override fun onConnected() {
                connectedCalled = true
            }

            override fun onDisconnected() {}
            override fun onError(errorCode: Int, errorMessage: String) {}
            override fun onDpDataReceived(dpMap: Map<Int, Any>) {}
        }

        val result = sdkManager.connectDevice("AA:BB:CC:DD:EE:FF", listener)

        assertThat(result).isTrue()
        assertThat(sdkManager.isConnected()).isTrue()
        assertThat(connectedCalled).isTrue()
    }

    @Test
    fun shouldUpdateConnectionStateAndNotifyListenerOnDisconnect() {
        var disconnectedCalled = false
        val listener = object : TuyaBleSdkListener {
            override fun onConnected() {}
            override fun onDisconnected() {
                disconnectedCalled = true
            }

            override fun onError(errorCode: Int, errorMessage: String) {}
            override fun onDpDataReceived(dpMap: Map<Int, Any>) {}
        }

        sdkManager.connectDevice("AA:BB:CC:DD:EE:FF", listener)
        sdkManager.disconnectDevice()

        assertThat(sdkManager.isConnected()).isFalse()
        assertThat(disconnectedCalled).isTrue()
    }

    @Test
    fun shouldFailSendingDpCommandWhenNotConnected() {
        val sent = sdkManager.sendDpCommand(dpId = 1, value = true)

        assertThat(sent).isFalse()
    }

    @Test
    fun shouldSendDpCommandSuccessfullyWhenConnected() {
        sdkManager.connectDevice("AA:BB:CC:DD:EE:FF")

        val sent = sdkManager.sendDpCommand(dpId = 1, value = true)

        assertThat(sent).isTrue()
    }

    @Test
    fun shouldNotifyListenerWhenIncomingDpDataReceived() {
        var receivedDpMap: Map<Int, Any>? = null
        val listener = object : TuyaBleSdkListener {
            override fun onConnected() {}
            override fun onDisconnected() {}
            override fun onError(errorCode: Int, errorMessage: String) {}
            override fun onDpDataReceived(dpMap: Map<Int, Any>) {
                receivedDpMap = dpMap
            }
        }

        sdkManager.setListener(listener)
        val testData = mapOf<Int, Any>(1 to true, 5 to 25)
        sdkManager.handleIncomingDpData(testData)

        assertThat(receivedDpMap).isEqualTo(testData)
    }
}
