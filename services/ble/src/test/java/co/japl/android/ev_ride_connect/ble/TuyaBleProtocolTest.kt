package co.japl.android.ev_ride_connect.ble

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TuyaBleProtocolTest {

    @Test
    fun shouldEncodeBooleanDpCommandCorrectly() {
        // DP1 (Lock = true) -> Boolean DP
        val encoded = TuyaBleProtocol.encodeDpCommand(dpId = 1, value = true)

        assertThat(encoded.size).isEqualTo(12)
        assertThat(encoded[0]).isEqualTo(0x55.toByte())
        assertThat(encoded[1]).isEqualTo(0xAA.toByte())
        assertThat(encoded[2]).isEqualTo(0x00.toByte()) // Version
        assertThat(encoded[3]).isEqualTo(0x06.toByte()) // CMD_DP_SEND
        assertThat(encoded[4]).isEqualTo(0x00.toByte()) // Len high byte
        assertThat(encoded[5]).isEqualTo(0x05.toByte()) // Len low byte (dpId:1 + dpType:1 + dpLen:2 + val:1 = 5)
        assertThat(encoded[6]).isEqualTo(0x01.toByte()) // DP ID 1
        assertThat(encoded[7]).isEqualTo(0x01.toByte()) // DP TYPE Boolean
        assertThat(encoded[8]).isEqualTo(0x00.toByte()) // DP Len high
        assertThat(encoded[9]).isEqualTo(0x01.toByte()) // DP Len low
        assertThat(encoded[10]).isEqualTo(0x01.toByte()) // Value true (1)

        val expectedChecksum = TuyaBleProtocol.calculateChecksum(encoded.copyOf(11))
        assertThat(encoded[11]).isEqualTo(expectedChecksum)
    }

    @Test
    fun shouldEncodeNumericDpCommandCorrectly() {
        // DP2 (Speed Mode = 2) -> Numeric/Value DP
        val encoded = TuyaBleProtocol.encodeDpCommand(dpId = 2, value = 2)

        assertThat(encoded.size).isEqualTo(15) // 6 header/len + (1 + 1 + 2 + 4) data + 1 checksum
        assertThat(encoded[0]).isEqualTo(0x55.toByte())
        assertThat(encoded[1]).isEqualTo(0xAA.toByte())
        assertThat(encoded[6]).isEqualTo(0x02.toByte()) // DP ID 2
        assertThat(encoded[7]).isEqualTo(0x02.toByte()) // DP TYPE Value
        assertThat(encoded[8]).isEqualTo(0x00.toByte())
        assertThat(encoded[9]).isEqualTo(0x04.toByte()) // Value DP len 4
        assertThat(encoded[13]).isEqualTo(0x02.toByte()) // Value 2
    }

    @Test
    fun shouldDecodeDpFrameWithBooleanAndNumericTypes() {
        // Construct frame with DP1 = true, DP5 = 25 (speed)
        val dp1 = byteArrayOf(0x01, 0x01, 0x00, 0x01, 0x01) // DP1, Boolean, len 1, true
        val dp5 = byteArrayOf(0x05, 0x02, 0x00, 0x04, 0x00, 0x00, 0x00, 0x19) // DP5, Value, len 4, 25

        val data = dp1 + dp5
        val dataLen = data.size // 5 + 8 = 13 (0x000D)

        val header = byteArrayOf(
            0x55.toByte(), 0xAA.toByte(), 0x00, 0x07,
            0x00, dataLen.toByte()
        )
        val frameWithoutChecksum = header + data
        val checksum = TuyaBleProtocol.calculateChecksum(frameWithoutChecksum)
        val fullFrame = frameWithoutChecksum + byteArrayOf(checksum)

        val decodedMap = TuyaBleProtocol.decodeDpFrame(fullFrame)

        assertThat(decodedMap).containsEntry(1, true)
        assertThat(decodedMap).containsEntry(5, 25)
    }

    @Test
    fun shouldReturnEmptyMapIfChecksumIsInvalid() {
        val dp1 = byteArrayOf(0x01, 0x01, 0x00, 0x01, 0x01)
        val header = byteArrayOf(0x55.toByte(), 0xAA.toByte(), 0x00, 0x07, 0x00, 0x05)
        val badChecksumFrame = header + dp1 + byteArrayOf(0x00) // Invalid checksum byte

        val decodedMap = TuyaBleProtocol.decodeDpFrame(badChecksumFrame)

        assertThat(decodedMap).isEmpty()
    }

    @Test
    fun shouldReturnEmptyMapIfHeaderIsInvalid() {
        val invalidHeaderFrame = byteArrayOf(0x00, 0x00, 0x00, 0x07, 0x00, 0x01, 0x01, 0x00)

        val decodedMap = TuyaBleProtocol.decodeDpFrame(invalidHeaderFrame)

        assertThat(decodedMap).isEmpty()
    }
}
