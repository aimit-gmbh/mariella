package org.mariella.persistence.kotlin.internal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mariella.persistence.kotlin.CachedSequence
import org.mariella.persistence.kotlin.Mapper

private class SequenceCache(private val name: String, private val incrementBy: Int) {
    private var current: Long? = null
    private var nextSelectAt: Long? = null

    init {
        if (incrementBy <= 1) error("increment must be > 1")
    }

    suspend fun next(mapper: Mapper): Long {
        if (current == null) {
            current = queryForSequence(mapper)
            nextSelectAt = current!! + incrementBy
        } else {
            current = if (current!! + 1 == nextSelectAt!!) {
                queryForSequence(mapper)
            } else {
                current!! + 1
            }
        }
        return current!!
    }

    private suspend fun queryForSequence(mapper: Mapper): Long =
        mapper.selectOneExistingPrimitive("select NEXTVAL('$name')")
}

internal class ThreadSafeCachedSequence(name: String, incrementBy: Int) : CachedSequence {
    private val mutex = Mutex()
    private val cache = SequenceCache(name, incrementBy)
    override suspend fun next(mapper: Mapper): Long {
        return mutex.withLock {
            cache.next(mapper)
        }
    }
}