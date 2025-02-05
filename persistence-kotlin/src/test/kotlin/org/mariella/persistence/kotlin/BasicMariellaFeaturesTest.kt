package org.mariella.persistence.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.entities.*
import org.mariella.persistence.kotlin.util.*
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.Instant
import java.util.*

class BasicMariellaFeaturesTest : AbstractDatabaseTest() {

    @Test
    fun `can create object`() {
        runTest {
            val session = database.createSession()
            val context = session.modify()

            val space = context.addExisting<Space>(TestData.TEST_SPACE)
            val user = context.addExisting<UserEntity>(TestData.USER_SEPPI)

            val revision = context.create<Revision> {
                it.space = space
                it.creationUser = user
                it.createdAt = Instant.now()
            }

            val file = context.create<File>()
            file.space = space
            file.comment = "my comment"
            file.owner = user
            file.revision = revision
            file.createdAt = revision.createdAt
            file.entityId = "entityId-1"

            val fileVersion = context.create<FileVersion>()
            fileVersion.name = "my file"
            fileVersion.space = space
            fileVersion.path = "/my/file/ola"
            fileVersion.revision = revision
            fileVersion.size = 100
            fileVersion.resource = file
            fileVersion.revisionFrom = revision.createdAt
            fileVersion.versionId = file.entityId + "-1"

            context.flush()
            session.commitAndClose()

            checkCountOfTable("resource_node", 1)
            checkCountOfTable("resource_node_version", 1)
            checkCountOfTable("file_node", 1)
            checkCountOfTable("file_version", 1)
        }
    }

    @Test
    fun `throws database exception when persisting`() {
        runTest {
            database.write {
                val c = modify()
                c.create<Space> {
                    it.name = "hansi"
                    it.securityConcept = SecurityConcept.Space
                }
                c.flush()
            }
            expectThrows<DatabaseException> {
                database.write {
                    val c = modify()
                    c.create<Space> {
                        it.name = "hansi"
                        it.securityConcept = SecurityConcept.Space
                    }
                    c.flush()
                }
            }
        }
    }

    @Test
    fun `can update object`() {
        runTest {
            val fileId = createFiles(1).single().id
            database.write {
                val context = modify()
                context.updateOne<FileVersion>(fileId) {
                    it.name = "asdasdasdasdasd"
                }
                context.flush()
            }

            database.read {
                val entity = modify().loadEntity<FileVersion>(fileId)!!
                expectThat(entity.name).isEqualTo("asdasdasdasdasd")
            }
        }
    }

    @Test
    fun `flush works with no modification`() {
        runTest {
            database.write {
                val context = modify()
                context.flush()
            }
        }
    }

    @Test
    fun `can delete object`() {
        runTest {
            val fileId = createFiles(1).single().id
            database.write {
                val context = modify()
                context.delete<FileVersion>(fileId)
                context.flush()
            }

            database.read {
                val entity = modify().loadEntity<FileVersion>(fileId)
                expectThat(entity).isNull()
            }
        }
    }

    @Test
    fun `can get sequence value`() {
        runTest {
            val session = database.createSession()
            val mod = session.modify()
            val seq = mod.sequenceNextValue("entity_id_seq")
            expectThat(seq).isEqualTo(1)
            session.close()
        }
    }

    @Test
    fun `can get cached sequence value`() {
        runTest {
            val session = database.createSession()
            val mod = session.modify()
            val seqValues = 1.rangeTo(1005).map {
                mod.cachedSequenceNextValue("cached_entity_id_seq")
            }
            expectThat(seqValues).hasSize(1005)
            session.close()
        }
    }

    @Test
    fun `can load an object shallow and not shallow`() {
        runTest {
            val fileVersionId = createFiles().single().id

            val session = database.createSession()
            val modifications = session.modify()
            val fileVersionShallow = modifications.loadEntity<FileVersion>(fileVersionId)!!

            expectThat(fileVersionShallow.revision).isNull()
            expectThat(fileVersionShallow.resource).isNull()

            val fileVersion = modifications.loadEntity<FileVersion>(
                fileVersionId,
                "root",
                "root.revision",
                "root.resource"
            )!!

            expectThat(fileVersion.revision).isNotNull()
            expectThat(fileVersion.resource).isNotNull()

            session.close()
        }
    }

    @Test
    fun `can modify single properties`() {
        runTest {
            val fileVersionId = createFiles().single().id

            val session = database.createSession()
            val context = session.modify()
            val fileVersion = context.loadEntity<FileVersion>(fileVersionId)!!

            fileVersion.size = 12
            fileVersion.path = "hansi"

            context.flush()
            session.commitAndClose()

            database.read {
                val entity = modify().loadEntity<FileVersion>(fileVersionId)!!
                expectThat(entity.size).isEqualTo(12)
                expectThat(fileVersion.path).isEqualTo("hansi")
            }
        }
    }

    @Test
    fun `can load multiple objects`() {
        runTest {
            val fileVersionIds = createFiles(3).map { it.id }

            val session = database.createSession()
            val context = session.modify()
            val fileVersions = context.loadEntities<FileVersion>(fileVersionIds)

            expectThat(fileVersions).hasSize(3)
            session.close()
        }
    }

    @Test
    fun `fails when updating a non existing entity`() {
        runTest {
            val session = database.createSession()
            val context = session.modify()
            expectThrows<RuntimeException> {
                context.updateOne<FileVersion>(UUID.randomUUID()) {
                    it.name = "asdasdasdasdasd"
                }
            }
        }
    }

    @Test
    fun `can load polymorphic object`() {
        runTest {
            val session = database.createSession()
            val mod = session.modify()

            val e = mod.create<Group> {
                it.name = "test group"
                it.members.add(addExisting<UserEntity>(TestData.USER_SEPPI, "U"))
                it.members.add(addExisting<UserEntity>(TestData.USER_KARL, "U"))
            }
            mod.flush()
            session.commitAndClose()

            database.read {
                val context = modify()
                val entity = context.loadEntity<Member>(e.id)
                expectThat(((entity as Group).name)).isEqualTo("test group")
                val entity1 = context.loadEntity<Member>(TestData.USER_SEPPI)
                expectThat(((entity1 as UserEntity).name)).isEqualTo("Seppi")
            }

            checkCountOfTable("auth_group", 3)
            checkCountOfTable("auth_membership", 2)
        }
    }

    @Test
    fun `supports ManyToMany mapping`() {
        runTest {
            val fileVersions = createFiles(10)

            val session = database.createSession()

            val modifications = session.modify()

            val relations = modifications.create<ParentalRelations>()

            relations.inputs.add(fileVersions[1])
            relations.inputs.add(fileVersions[3])
            relations.inputs.add(fileVersions[4])

            relations.outputs.add(fileVersions[2])
            relations.outputs.add(fileVersions[6])
            relations.outputs.add(fileVersions[8])

            modifications.flush()
            session.commitAndClose()

            val relationId = relations.id

            checkCountOfTable("parental_relation", 1)
            checkCountOfTable("parental_input", 3)
            checkCountOfTable("parental_output", 3)

            val newSession = database.createSession()
            val newModifications = newSession.modify()
            val loadedEntity = newModifications.loadEntity<ParentalRelations>(
                relationId,
                "root",
                "root.inputs",
                "root.outputs"
            )!!
            expectThat(loadedEntity.inputs).hasSize(3)
            expectThat(loadedEntity.outputs).hasSize(3)
            newSession.close()
        }
    }

}