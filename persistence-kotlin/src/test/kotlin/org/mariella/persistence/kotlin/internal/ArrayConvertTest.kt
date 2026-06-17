package org.mariella.persistence.kotlin.internal

import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class ArrayConvertTest {

    @Test
    fun `sets correct value`() {
        val parameterValues = VertxParameterValues()
        arrayConverter.setObject(parameterValues, 1, arrayOf(1, 2, 3))
        expectThat(parameterValues.tuple().getArrayOfIntegers(0)).isEqualTo(arrayOf(1, 2, 3))
    }

    @Test
    fun `all other methods are not implemented`() {
        expectThrows<UnsupportedOperationException> { arrayConverter.getObject(mockk(), 0) }
        expectThrows<UnsupportedOperationException> { arrayConverter.createLiteral(1) }
        expectThrows<UnsupportedOperationException> { arrayConverter.createDummy() }
        expectThrows<UnsupportedOperationException> { arrayConverter.toString(arrayOf(1, 2, 3)) }
    }
}