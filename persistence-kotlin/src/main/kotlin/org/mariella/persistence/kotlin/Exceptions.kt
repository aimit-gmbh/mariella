@file:Suppress("UNCHECKED_CAST")

package org.mariella.persistence.kotlin

class DatabaseException(msg: String) : RuntimeException(msg) {
    inline fun <reified T : Throwable> unwrapOrThrow(): T {
        var localCause = cause
        while (true) {
            if (localCause == null) throw this
            if (localCause is T)
                return localCause
            else
                localCause = localCause.cause
        }
        throw this
    }
}