package org.mariella.persistence.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.entities.Group
import org.mariella.persistence.kotlin.util.AbstractDatabaseTest
import org.mariella.persistence.kotlin.util.TestEnvironment
import org.mariella.persistence.kotlin.util.createPool
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class IsolationTest : AbstractDatabaseTest() {

    @Test
    fun `sessions are isolated`() {
        runTest {
            val session1 = database.createSession()
            val mod1 = session1.modify()
            val group = mod1.create<Group> {
                it.name = "hansi"
            }
            mod1.flush()

            val session2 = database.createSession()
            val mod2 = session2.modify()
            val loaded = mod2.loadEntity<Group>(group.id)
            expectThat(loaded).isNull()

            session1.commit()

            val loaded1 = mod2.loadEntity<Group>(group.id)
            expectThat(loaded1).isNotNull()

            session1.close()
            session2.close()
        }
    }

    @Test
    fun `auto commit mode works`() {
        runTest {
            val autoCommitDatabase = TestEnvironment.createDatabase(createPool(vertx, dbConfig, true), dbConfig)
            val session1 = autoCommitDatabase.createSession(true)
            val mod1 = session1.modify()
            val group = mod1.create<Group> {
                it.name = "hansi"
            }
            mod1.flush()

            val session2 = autoCommitDatabase.createSession()
            val mod2 = session2.modify()
            val loaded = mod2.loadEntity<Group>(group.id)
            expectThat(loaded).isNotNull()

            session1.close()
            session2.close()
        }
    }
}