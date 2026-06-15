package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.Converter
import org.mariella.persistence.kotlin.TimestampInstantConverter
import org.mariella.persistence.query.Literal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class InstantLiteral : Literal<Instant?> {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss:SSSSSS'Z'")
        private val zone: ZoneId = ZoneId.of("UTC")
    }
    @Suppress("unused")
    constructor(value: Instant?) : super(TimestampInstantConverter, value)
    constructor(converter: Converter<Instant?>?, value: Instant?) : super(converter, value)

    // this does not work for h2 databases because the returned timezone of to_timestamp is wrong
    override fun printSql(b: StringBuilder) {
        val curVal = value
        if (curVal == null) {
            b.append("NULL")
        } else {
            val nanosAfterMicro = curVal.nano % 1000
            val rounded = if (nanosAfterMicro > 499) {
                curVal.plusNanos((1000 - nanosAfterMicro).toLong())
            } else curVal
            val string = OffsetDateTime.ofInstant(rounded, zone).format(formatter)
            b.append("TO_TIMESTAMP('")
            b.append(string)
            b.append("','YYYY-MM-DD\"T\"HH24:MI:SS:US\"Z\"')")
        }
    }
}

