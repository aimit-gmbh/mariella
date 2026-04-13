package org.mariella.persistence.kotlin.internal

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mariella.persistence.kotlin.CachedSequence
import org.mariella.persistence.kotlin.Mapper

internal abstract class AbstractSequenceCache(protected val name: String, protected val incrementBy: Int) {
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

    abstract suspend fun queryForSequence(mapper: Mapper): Long
}

internal class SequenceCache(name: String, incrementBy: Int) : AbstractSequenceCache(name, incrementBy) {
    override suspend fun queryForSequence(mapper: Mapper): Long =
        mapper.selectOneExistingPrimitive("select NEXTVAL('$name')")
}

internal class PostegresSequenceCache(name: String, incrementBy: Int) : AbstractSequenceCache(name, incrementBy) {
    override suspend fun queryForSequence(mapper: Mapper): Long =
        mapper.selectPrimitive<Long>("select NEXTVAL('$name') from generate_series(1,$incrementBy)").first()
}

internal class ThreadSafeCachedSequence(private val cache: AbstractSequenceCache) : CachedSequence {
    private val mutex = Mutex()

    override suspend fun next(mapper: Mapper): Long {
        return mutex.withLock {
            cache.next(mapper)
        }
    }
}