package org.mariella.persistence.kotlin

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.entities.*
import org.mariella.persistence.kotlin.internal.LoadByIdProvider
import org.mariella.persistence.kotlin.util.*
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*
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
    fun `can handle tables in other schemas`() {
        runTest {
            val id = database.write {
                val context = modify()

                val tab = context.create<OtherSchema>()
                tab.name = "hansi"

                context.flush()

                tab.id
            }
            database.read {
                val context = modify()

                val loaded = context.loadEntity<OtherSchema>(id)!!
                expectThat(loaded.name).isEqualTo("hansi")

                val loadedId = context.mapper.selectOneExistingPrimitive<UUID>("select id from hansi.other_schema")
                expectThat(loadedId).isEqualTo(id)
            }
        }
    }

    @Test
    fun `can map one to many`() {
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

            context.flush()
            val newContext = session.modify()
            val loadedFile = newContext.loadEntity<File>(file.id, "root", "root.resourceVersions")
            expectThat(loadedFile!!.resourceVersions).isEmpty()

            val fileVersion = newContext.create<FileVersion>()
            fileVersion.name = "my file"
            fileVersion.space = space
            fileVersion.path = "/my/file/ola"
            fileVersion.revision = revision
            fileVersion.size = 100
            fileVersion.resource = file
            fileVersion.revisionFrom = revision.createdAt
            fileVersion.versionId = file.entityId + "-1"

            loadedFile.resourceVersions.add(fileVersion)
            newContext.flush()

            val newestContext = session.modify()
            val newLoadedFile = newestContext.loadEntity<File>(file.id, "root.resourceVersions")
            expectThat(newLoadedFile!!.resourceVersions).hasSize(1)

            val fileVersion2 = newestContext.create<FileVersion>()
            fileVersion2.name = "my file"
            fileVersion2.space = space
            fileVersion2.path = "/my/file/ola"
            fileVersion2.revision = revision
            fileVersion2.size = 100
            fileVersion2.resource = file
            fileVersion2.revisionFrom = revision.createdAt
            fileVersion2.revisionTo = Instant.now()
            fileVersion2.versionId = file.entityId + "-1"
            newLoadedFile.resourceVersions.add(fileVersion2)
            newestContext.flush()

            val fileWith2 = newestContext.loadEntity<File>(file.id, "root", "root.resourceVersions")
            expectThat(newLoadedFile.resourceVersions).hasSize(2)

            newestContext.delete<FileVersion>(fileWith2!!.resourceVersions[0].id)
            newestContext.flush()
            newestContext.loadEntity<File>(file.id, "root", "root.resourceVersions")
            expectThat(newLoadedFile.resourceVersions).hasSize(1)

            val version = fileWith2.resourceVersions.removeFirst()
            expectThat(version.resource).isNull()
            expectThrows<DatabaseException> { newestContext.flush() }
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
                    it.hash = byteArrayOf(4, 5, 6)
                }
                context.flush()
            }

            database.read {
                val entity = modify().loadEntity<FileVersion>(fileId)!!
                expectThat(entity.name).isEqualTo("asdasdasdasdasd")
                expectThat(entity.hash).isEqualTo(byteArrayOf(4, 5, 6))
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
                val entities = modify().load<FileVersion>(conditionProvider = LoadByIdProvider(fileId))
                expectThat(entities).isEmpty()
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
            expectThat(fileVersionShallow.hash).isEqualTo(byteArrayOf(1, 2, 3))

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
    fun `can load with own provider`() {
        runTest {
            val fileVersionId = createFiles().single().id

            val session = database.createSession()
            val modifications = session.modify()
            val fileVersionShallow = modifications.load<FileVersion>(conditionProvider = LoadByIdProvider(fileVersionId)).single()

            expectThat(fileVersionShallow.revision).isNull()
            expectThat(fileVersionShallow.resource).isNull()

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
            val fileVersions = context.loadEntities<FileVersion>(fileVersionIds, "root", "root.revision", "root.space")

            expectThat(fileVersions).hasSize(3)
            fileVersions.forEach {
                expectThat(it.space).isNotNull()
                expectThat(it.revision).isNotNull()
            }

            val newContext = session.modify()
            val emptyFileVersion = newContext.loadEntities<FileVersion>(fileVersionIds)
            expectThat(emptyFileVersion).hasSize(3)
            emptyFileVersion.forEach {
                expectThat(it.space).isNull()
                expectThat(it.revision).isNull()
            }

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