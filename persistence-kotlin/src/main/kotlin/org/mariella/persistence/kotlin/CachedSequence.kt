package org.mariella.persistence.kotlin

import org.mariella.persistence.kotlin.internal.ThreadSafeCachedSequence

interface CachedSequence {
    companion object {
        fun of(name: String, incrementBy: Int): CachedSequence = ThreadSafeCachedSequence(name, incrementBy)
    }

    suspend fun next(mapper: Mapper): Long
}