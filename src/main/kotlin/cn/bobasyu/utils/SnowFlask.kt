package cn.bobasyu.utils

private const val EPOCH: Long = 1_609_459_200_000L // 2021-01-01 00:00:00 UTC
private const val TIMESTAMP_BITS = 41
private const val MACHINE_ID_BITS = 10
private const val SEQUENCE_BITS = 12

private const val MAX_MACHINE_ID = (1L shl MACHINE_ID_BITS) - 1
private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1

private const val MACHINE_ID_SHIFT = SEQUENCE_BITS
private const val TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS
private const val TIMESTAMP_MASK = (1L shl TIMESTAMP_BITS) - 1

class Snowflake(private val machineId: Int) {
    private var lastTimestamp = -1L
    private var sequence = 0L

    init {
        require(machineId in 0..MAX_MACHINE_ID) { "Machine ID must be between 0 and $MAX_MACHINE_ID" }
    }

    fun nextId(): Long {
        var timestamp = timeGen()
        if (timestamp < lastTimestamp) {
            throw IllegalArgumentException("Clock moved backwards. Refusing to generate id for ${lastTimestamp - timestamp} milliseconds")
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                timestamp = tilNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0L
        }

        lastTimestamp = timestamp

        return ((timestamp - EPOCH) shl TIMESTAMP_LEFT_SHIFT) or
                (machineId.toLong() shl MACHINE_ID_SHIFT) or
                sequence
    }

    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = timeGen()
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen()
        }
        return timestamp
    }

    private fun timeGen(): Long {
        return System.currentTimeMillis()
    }
}

private val snowflake by lazy { Snowflake(123) }

fun generateId() = snowflake.nextId()