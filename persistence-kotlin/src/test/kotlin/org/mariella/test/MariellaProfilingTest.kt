package org.mariella.test

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mariella.test.entities.FileVersion
import org.mariella.test.util.AbstractDatabaseTest
import org.mariella.test.util.createFiles
import strikt.api.expectThat
import strikt.assertions.hasSize

class MariellaProfilingTest : AbstractDatabaseTest() {

    @Disabled
    @Test
    fun `profile mariella`() {
        runTest {
            val nrOfFiles = 10_000
            val files = createFiles(nrOfFiles)

            val session = database.createSession()
            val modifications = session.modify()
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

            session.rollbackAndClose()
        }
    }
}