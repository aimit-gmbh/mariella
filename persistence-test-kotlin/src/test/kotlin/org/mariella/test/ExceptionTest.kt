package org.mariella.test

import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.DatabaseException
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isSameInstanceAs

class ExceptionTest {

    @Test
    fun `throws if underlying exception is of wrong type`() {
        val ex = DatabaseException("bla")
        expectThrows<DatabaseException> { ex.unwrapOrThrow<IllegalStateException>() }.isSameInstanceAs(ex)
    }

    @Test
    fun `unwraps cause`() {
        val ex = DatabaseException("bla")
        val other = IllegalStateException("bla")
        ex.initCause(other)
        expectThat(ex.unwrapOrThrow<IllegalStateException>()).isSameInstanceAs(other)
    }

    @Test
    fun `unwrap nested exception`() {
        val ex = DatabaseException("bla")
        val nested = IndexOutOfBoundsException("bla")
        val other = IllegalStateException("bla", IllegalArgumentException("muh", nested))
        ex.initCause(other)
        expectThat(ex.unwrapOrThrow<IndexOutOfBoundsException>()).isSameInstanceAs(nested)
    }

    @Test
    fun `unwraps only first exception`() {
        val ex = DatabaseException("bla")
        val other = IllegalStateException("bla", IllegalStateException("muh"))
        ex.initCause(other)
        expectThat(ex.unwrapOrThrow<IllegalStateException>()).isSameInstanceAs(other)
    }

    @Test
    fun `does not produce an endless loop on circular dependencies`() {
        val ex = DatabaseException("bla")
        val other = IllegalStateException("bla")
        other.initCause(ex)
        ex.initCause(other)
        expectThrows<DatabaseException> { ex.unwrapOrThrow<IllegalArgumentException>() }.isSameInstanceAs(ex)
    }
}