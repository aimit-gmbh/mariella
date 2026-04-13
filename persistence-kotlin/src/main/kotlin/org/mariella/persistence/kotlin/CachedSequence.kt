package org.mariella.persistence.kotlin

import org.mariella.persistence.kotlin.internal.PostegresSequenceCache
import org.mariella.persistence.kotlin.internal.SequenceCache
import org.mariella.persistence.kotlin.internal.ThreadSafeCachedSequence

interface CachedSequence {
    companion object {
        fun usingIncrementBy(name: String, incrementBy: Int): CachedSequence = ThreadSafeCachedSequence(SequenceCache(name, incrementBy))
        fun usingGenerateSequence(name: String, incrementBy: Int): CachedSequence = ThreadSafeCachedSequence(PostegresSequenceCache(name, incrementBy))
    }

    suspend fun next(mapper: Mapper): Long
}