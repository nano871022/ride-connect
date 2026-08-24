package co.japl.android.ev_ride_connect.ble

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

object TuyaBleProtocol {

    // Tuya BLE Service and Characteristic UUIDs
    val TUYA_SERVICE_UUID: UUID = UUID.fromString("0000FD50-0000-1000-8000-00805F9B34FB")
    val TUYA_WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FD51-0000-1000-8000-00805F9B34FB")
    val TUYA_NOTIFY_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FD52-0000-1000-8000-00805F9B34FB")

    // Alternative standard transparent BLE UUIDs (for compatibility with scooters using standard serial bridge)
    val ALT_SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    val ALT_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")

    const val HEADER_1: Byte = 0x55.toByte()
    const val HEADER_2: Byte = 0xAA.toByte()

    const val CMD_DP_SEND: Byte = 0x06
    const val CMD_DP_REPORT: Byte = 0x07
    const val CMD_DP_QUERY: Byte = 0x08

    const val DP_TYPE_BOOLEAN: Byte = 0x01
    const val DP_TYPE_VALUE: Byte = 0x02
    const val DP_TYPE_STRING: Byte = 0x03
    const val DP_TYPE_ENUM: Byte = 0x04
    const val DP_TYPE_BITMAP: Byte = 0x05

    /**
     * Encodes a DP command into a complete Tuya BLE packet byte array.
     */
    fun encodeDpCommand(dpId: Int, value: Any, version: Byte = 0x00): ByteArray {
        val dpData = encodeDpData(dpId, value)
        val dataLen = dpData.size

        // Frame format: Header (2) + Version (1) + Command (1) + DataLength (2) + Data (N) + Checksum (1)
        val buffer = ByteBuffer.allocate(2 + 1 + 1 + 2 + dataLen + 1)
        buffer.order(ByteOrder.BIG_ENDIAN)

        buffer.put(HEADER_1)
        buffer.put(HEADER_2)
        buffer.put(version)
        buffer.put(CMD_DP_SEND)
        buffer.putShort(dataLen.toShort())
        buffer.put(dpData)

        val packetWithoutChecksum = buffer.array().copyOf(buffer.position())
        val checksum = calculateChecksum(packetWithoutChecksum)
        buffer.put(checksum)

        return buffer.array()
    }

    /**
     * Decodes a Tuya BLE packet byte array into a map of DP IDs to their values.
     * Returns an empty map if payload header or checksum is invalid.
     */
    fun decodeDpFrame(frame: ByteArray): Map<Int, Any> {
        if (frame.size < 7) return emptyMap()

        if (frame[0] != HEADER_1 || frame[1] != HEADER_2) {
            return emptyMap()
        }

        val frameWithoutChecksum = frame.copyOf(frame.size - 1)
        val expectedChecksum = calculateChecksum(frameWithoutChecksum)
        val actualChecksum = frame[frame.size - 1]

        if (expectedChecksum != actualChecksum) {
            return emptyMap()
        }

        val buffer = ByteBuffer.wrap(frame).order(ByteOrder.BIG_ENDIAN)
        buffer.position(2) // Skip header

        val version = buffer.get()
        val command = buffer.get()
        val dataLength = buffer.short.toInt() and 0xFFFF

        if (buffer.remaining() < dataLength + 1) {
            return emptyMap()
        }

        val dpMap = mutableMapOf<Int, Any>()
        var bytesParsed = 0

        while (bytesParsed < dataLength && buffer.hasRemaining()) {
            val dpId = buffer.get().toInt() and 0xFF
            val dpType = buffer.get()
            val dpLen = buffer.short.toInt() and 0xFFFF

            if (buffer.remaining() < dpLen + 1) break // Prevent buffer overflow

            val valueBytes = ByteArray(dpLen)
            buffer.get(valueBytes)

            val parsedValue: Any? = when (dpType) {
                DP_TYPE_BOOLEAN -> valueBytes.getOrNull(0) == 0x01.toByte()
                DP_TYPE_VALUE -> {
                    val valueBuffer = ByteBuffer.wrap(valueBytes).order(ByteOrder.BIG_ENDIAN)
                    when (dpLen) {
                        1 -> valueBytes[0].toInt() and 0xFF
                        2 -> valueBuffer.short.toInt() and 0xFFFF
                        4 -> valueBuffer.int
                        else -> 0
                    }
                }
                DP_TYPE_ENUM -> valueBytes.getOrNull(0)?.toInt() ?: 0
                DP_TYPE_STRING -> String(valueBytes, Charsets.UTF_8)
                else -> valueBytes
            }

            if (parsedValue != null) {
                dpMap[dpId] = parsedValue
            }

            bytesParsed += 1 + 1 + 2 + dpLen
        }

        return dpMap
    }

    private fun encodeDpData(dpId: Int, value: Any): ByteArray {
        return when (value) {
            is Boolean -> {
                val buffer = ByteBuffer.allocate(1 + 1 + 2 + 1)
                buffer.order(ByteOrder.BIG_ENDIAN)
                buffer.put(dpId.toByte())
                buffer.put(DP_TYPE_BOOLEAN)
                buffer.putShort(1)
                buffer.put(if (value) 0x01 else 0x00)
                buffer.array()
            }
            is Number -> {
                val intVal = value.toInt()
                val buffer = ByteBuffer.allocate(1 + 1 + 2 + 4)
                buffer.order(ByteOrder.BIG_ENDIAN)
                buffer.put(dpId.toByte())
                buffer.put(DP_TYPE_VALUE)
                buffer.putShort(4)
                buffer.putInt(intVal)
                buffer.array()
            }
            else -> ByteArray(0)
        }
    }

    fun calculateChecksum(bytes: ByteArray): Byte {
        var sum = 0
        for (b in bytes) {
            sum += (b.toInt() and 0xFF)
        }
        return (sum % 256).toByte()
    }
}
