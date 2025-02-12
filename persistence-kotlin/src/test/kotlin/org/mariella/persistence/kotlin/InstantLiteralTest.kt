package org.mariella.persistence.kotlin

import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.entities.Entity
import org.mariella.persistence.kotlin.internal.InstantLiteral
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant

class InstantLiteralTest {

    @Test
    fun `instant literal creates correct where clause`() {
        expectThat(buildString {
            InstantLiteral(
                Instant.ofEpochSecond(
                    1739370666,
                    21299400
                )
            ).printSql(this)
        }).isEqualTo("TO_TIMESTAMP('2025-02-12T14:31:06:021299Z','YYYY-MM-DD\"T\"HH24:MI:SS:US\"Z\"')")
        expectThat(buildString {
            InstantLiteral(
                Instant.ofEpochSecond(
                    1739370666,
                    21299600
                )
            ).printSql(this)
        }).isEqualTo("TO_TIMESTAMP('2025-02-12T14:31:06:021299Z','YYYY-MM-DD\"T\"HH24:MI:SS:US\"Z\"')")
        expectThat(buildString {
            InstantLiteral(
                Instant.ofEpochSecond(
                    1739370666,
                    21299999
                )
            ).printSql(this)
        }).isEqualTo("TO_TIMESTAMP('2025-02-12T14:31:06:021299Z','YYYY-MM-DD\"T\"HH24:MI:SS:US\"Z\"')")
        expectThat(buildString {
            InstantLiteral(
                Instant.ofEpochSecond(
                    1739370666,
                    21299000
                )
            ).printSql(this)
        }).isEqualTo("TO_TIMESTAMP('2025-02-12T14:31:06:021299Z','YYYY-MM-DD\"T\"HH24:MI:SS:US\"Z\"')")
        expectThat(buildString {
            InstantLiteral(
                Entity.MAX_DB_TIMESTAMP
            ).printSql(this)
        }).isEqualTo("TO_TIMESTAMP('9999-12-31T00:00:00:000000Z','YYYY-MM-DD\"T\"HH24:MI:SS:US\"Z\"')")
    }
}