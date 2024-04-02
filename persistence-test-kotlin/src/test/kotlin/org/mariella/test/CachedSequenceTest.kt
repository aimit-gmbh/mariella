package org.mariella.test

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.CachedSequence
import org.mariella.persistence.kotlin.ThreadSafeCachedSequence
import org.mariella.test.util.AbstractDatabaseTest
import org.mariella.test.util.read
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

class CachedSequenceTest : AbstractDatabaseTest() {
    @Test
    fun `can cache sequence`() {
        runTest {
            database.read {
                val seq = CachedSequence("entity_id_seq", 1000, mapper)
                repeat(100_000) {
                    expectThat(seq.next().toInt()).isEqualTo(it + 1)
                }
            }
        }
    }

    @Test
    fun `keeps order when caching thread safe`() {
        runTest {
            database.read {
                val seq = ThreadSafeCachedSequence("entity_id_seq", 1000)
                val e = 1.rangeTo(100000).map { it.toLong() }
                val noThread = 1.rangeTo(100000).map {
                    seq.next(mapper)
                }
                expectThat(e).hasSize(100000)
                expectThat(noThread).isEqualTo(e)
            }
        }
    }

    @Test
    fun `selects all values when running async`() {
        runTest {
            database.read {
                val seq = ThreadSafeCachedSequence("entity_id_seq", 1000)
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
    }
}