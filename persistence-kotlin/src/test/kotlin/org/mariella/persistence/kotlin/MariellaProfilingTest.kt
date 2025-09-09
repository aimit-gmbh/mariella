package org.mariella.persistence.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.entities.FileVersion
import org.mariella.persistence.kotlin.util.AbstractDatabaseTest
import org.mariella.persistence.kotlin.util.createFiles
import strikt.api.expectThat
import strikt.assertions.hasSize

@Disabled
class MariellaProfilingTest : AbstractDatabaseTest() {


    @Test
    fun `profile mariella`() {
        runTest {
            val nrOfFiles = 10_000
            val files = createFiles(nrOfFiles)

            val session = database.connect()
            val modifications = session.mariella()
            repeat(100_000) {
                val versions =
                    modifications.loadEntities<FileVersion>(
                        files.map { it.id },
                        "root",
                        "root.resource",
                        "root.parent",
                        "root.space"
                    )
                expectThat(versions).hasSize(nrOfFiles)
            }

            session.close()
        }
    }
}