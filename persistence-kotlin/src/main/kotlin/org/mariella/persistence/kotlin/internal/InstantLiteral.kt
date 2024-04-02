package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.Converter
import org.mariella.persistence.kotlin.TimestampInstantConverter
import org.mariella.persistence.query.Literal
import java.time.Instant

internal class InstantLiteral : Literal<Instant?> {
    @Suppress("unused")
    constructor(value: Instant?) : super(TimestampInstantConverter, value)
    constructor(converter: Converter<Instant?>?, value: Instant?) : super(converter, value)
}

