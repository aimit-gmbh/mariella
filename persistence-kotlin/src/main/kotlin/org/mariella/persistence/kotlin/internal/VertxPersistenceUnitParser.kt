package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.annotations_processing.ClassLoaderPersistenceUnitParser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets

internal class VertxPersistenceUnitParser(classLoader: ClassLoader, private val persistenceXml: String) :
    ClassLoaderPersistenceUnitParser(classLoader) {
    override fun createPersistencUnitInputStream(): InputStream {
        return ByteArrayInputStream(persistenceXml.toByteArray(StandardCharsets.UTF_8))
    }

    override fun getRootUrl(url: URL?): URL {
        return URI("file:", null, File(".").path).toURL()
    }
}