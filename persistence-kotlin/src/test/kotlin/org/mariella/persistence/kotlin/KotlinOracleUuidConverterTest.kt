package org.mariella.persistence.kotlin

import io.mockk.*
import org.junit.jupiter.api.Test
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.util.UUIDUtils
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.*
import kotlin.uuid.toKotlinUuid

class KotlinOracleUuidConverterTest {

    @Test
    fun `can get object`() {
        val row = mockk<ResultRow>()
        val uuid = UUID.randomUUID()
        every { row.getBytes(any()) } returns UUIDUtils.toBytes(uuid)
        val fetched = KotlinOracleUuidConverter.getObject(row, 2)
        expectThat(fetched).isEqualTo(uuid.toKotlinUuid())
        verify { row.getBytes(2) }
    }

    @Test
    fun `can set object`() {
        val values = mockk<ParameterValues>()
        val uuid = UUID.randomUUID()
        every { values.setBytes(any(), any()) } just runs
        KotlinOracleUuidConverter.setObject(values, 3, uuid.toKotlinUuid())
        verify { values.setBytes(3, UUIDUtils.toBytes(uuid)) }
        KotlinOracleUuidConverter.setObject(values, 4, null)
        verify { values.setBytes(4, null) }
    }
}