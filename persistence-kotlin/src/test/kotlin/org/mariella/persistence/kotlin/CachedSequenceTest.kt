package org.mariella.persistence.kotlin

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.util.AbstractDatabaseTest
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class CachedSequenceTest : AbstractDatabaseTest() {
    @Test
    fun `can cache sequence`() {
        runTest {
            val seq = CachedSequence.usingIncrementBy("cached_entity_id_seq", 1000)
            checkBasicSequence(seq)
        }
    }

    @Test
    fun `can cache sequence with using generate sequence`() {
        runTest {
            val seq = CachedSequence.usingGenerateSequence("entity_id_seq", 1000)
            checkBasicSequence(seq)
        }
    }

    @Test
    fun `keeps order when caching thread safe`() {
        runTest {
            val seq = CachedSequence.usingIncrementBy("cached_entity_id_seq", 1000)
            checkOrder(seq)
        }
    }

    @Test
    fun `keeps order when using generate sequence`() {
        runTest {
            val seq = CachedSequence.usingGenerateSequence("entity_id_seq", 1000)
            checkOrder(seq)
        }
    }

    @Test
    fun `selects all values when running async`() {
        runTest {
            val seq = CachedSequence.usingIncrementBy("cached_entity_id_seq", 1000)
            checkAsync(seq)
        }
    }

    @Test
    fun `selects all values when running async and using generate sequence`() {
        runTest {
            val seq = CachedSequence.usingGenerateSequence("entity_id_seq", 1000)
            checkAsync(seq)
        }
    }

    private suspend fun TestScope.checkAsync(seq: CachedSequence) {
        database.read {
            val mapper = mapper()
            val data1 = async {
                1.rangeTo(50000).map {
                    seq.next(mapper)
                }
            }
            val data2 = async {
                1.rangeTo(50000).map {
                    seq.next(mapper)
                }
            }
            val asyncValues = listOf(data1, data2).awaitAll()
            asyncValues.forEach {
                expectThat(it.size == 50000)
            }

            val e = 1.rangeTo(100000).map { it.toLong() }
            expectThat(asyncValues.flatten().sorted()).isEqualTo(e)
        }
    }

    private suspend fun checkOrder(seq: CachedSequence) {
        database.read {
            val mapper = mapper()
            val e = 1.rangeTo(100000).map { it.toLong() }
            val noThread = 1.rangeTo(100000).map {
                seq.next(mapper)
            }
            expectThat(e).hasSize(100000)
            expectThat(noThread).isEqualTo(e)
        }
    }

    private suspend fun checkBasicSequence(seq: CachedSequence) {
        database.read {
            val mapper = mapper()
            repeat(100_000) {
                expectThat(seq.next(mapper).toInt()).isEqualTo(it + 1)
            }
        }
    }
}