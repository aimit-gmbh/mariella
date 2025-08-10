package org.mariella.persistence.kotlin

import io.vertx.pgclient.PgException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.entities.Entity
import org.mariella.persistence.kotlin.entities.ResourceType
import org.mariella.persistence.kotlin.entities.SecurityConcept
import org.mariella.persistence.kotlin.internal.InstantLiteral
import org.mariella.persistence.kotlin.util.*
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class MapperTest : AbstractDatabaseTest() {

    data class ClassWithStandardMappings(
        val id: UUID,
        val description: String?,
        val lockDate: Instant?
    )

    @JvmInline
    value class ResourceId(val uuid: UUID)

    data class ResourceWithId(val id: ResourceId, val description: String?)

    enum class TestEnum { A, B }

    data class ClassWithEnums(
        val enum1: TestEnum,
        val enum2: TestEnum
    )

    data class ClassWithSealedClasses(
        val id: UUID,
        val sec1: SecurityConcept,
        val sec2: SecurityConcept,
        val type: ResourceType
    )

    data class ByteArrayWrapper(val arr: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ByteArrayWrapper
            return arr.contentEquals(other.arr)
        }

        override fun hashCode(): Int {
            return arr.contentHashCode()
        }
    }

    data class ClassWithDefaults(
        val id: UUID,
        val description: String = "hansi",
    )

    data class ClassWithNullableAndDefault(
        val id: UUID? = UUID.randomUUID()
    )

    @Test
    fun `can map query without parameter`() {
        val sql = "select id, 'hello' as description, current_timestamp as lockDate from resource_node"

        runTest {
            createFiles(3)
            database.read {
                val listOfNodes = mapper().select<ClassWithStandardMappings>(sql)
                expectThat(listOfNodes).hasSize(3)
                listOfNodes.forEach {
                    expectThat(it.description).isEqualTo("hello")
                }
            }
        }
    }

    @Test
    fun `can map value classes`() {
        val sql = "select id, 'hello' as description from resource_node"

        runTest {
            createFiles(3)
            database.read {
                val listOfNodes = mapper().select<ResourceWithId>(sql)
                expectThat(listOfNodes).hasSize(3)
            }
        }
    }

    @Test
    fun `can use sealed class as parameter`() {
        val sql = "select security_concept from space where security_concept = $1"

        runTest {
            createFiles(1)
            database.read {
                val list = mapper().selectPrimitive<SecurityConcept>(sql, SecurityConcept.Public)
                expectThat(list.single()).isEqualTo(SecurityConcept.Public)
            }
        }
    }

    @Test
    fun `can pass value classes as parameter`() {
        val sql = "select id, 'hello' as description from resource_node where id = $1"

        runTest {
            val id = createFiles(1).single().resource!!.id
            database.read {
                val listOfNodes = mapper().select<ResourceWithId>(sql, ResourceId(id))
                expectThat(listOfNodes).hasSize(1)
            }
        }
    }

    @Test
    fun `can use instant as parameter`() {
        val sql = """
            select id, 'hello' as description, revision_to_time as lockDate from resource_node_version
            where revision_to_time = $1
        """.trimIndent()
        runTest {
            createFiles(1)
            val data = database.read {
                mapper().select<ClassWithStandardMappings>(sql, Entity.MAX_DB_TIMESTAMP)
            }
            expectThat(data.single().lockDate!!.toEpochMilli()).isEqualTo(Entity.MAX_DB_TIMESTAMP.toEpochMilli())
        }
    }

    @Test
    fun `can map byte array`() {
        val sql = """
            select file_hash as arr from file_version
            where id = $1
        """.trimIndent()
        runTest {
            val file = createFiles(1).single()
            val data = database.read {
                mapper().select<ByteArrayWrapper>(sql, file.id)
            }
            expectThat(data.single().arr).isEqualTo(byteArrayOf(1, 2, 3))
            expectThat(data.single()).isEqualTo(ByteArrayWrapper(byteArrayOf(1, 2, 3)))
        }
    }

    @Test
    fun `instant literal has correct where clause`() {
        assumeTrue(DATABASE_TYPE == DatabaseType.POSTGRES)
        runTest {
            val file = createFiles(1).single()
            val sql = """
                select id, 'hello' as description, revision_from_time as lockDate from resource_node_version
                where revision_to_time = ${buildString { InstantLiteral(Entity.MAX_DB_TIMESTAMP).printSql(this) }}
                and revision_from_time = ${buildString { InstantLiteral(file.revisionFrom).printSql(this) }}
            """.trimIndent()
            val data = database.read {
                mapper().select<ClassWithStandardMappings>(sql)
            }
            expectThat(data.single().lockDate!!).isEqualTo(file.revisionFrom.truncatedTo(ChronoUnit.MICROS))
        }
    }

    @Test
    fun `can get current timestamp from the db`() {
        runTest {
            database.read {
                val instant = mapper().selectOneExistingPrimitive<Instant>("select current_timestamp")
                expectThat(instant).isA<Instant>()
            }
        }
    }

    @Test
    fun `can map cursor`() {
        val sql = "select id, 'hello' as description, current_timestamp as lockDate from resource_node"

        runTest {
            createFiles(100)
            database.read {
                val cursor = mapper().cursor<ClassWithStandardMappings>(sql, batchSize = 9)
                val listOfNodes = cursor.toList()
                expectThat(listOfNodes).hasSize(100)
                listOfNodes.forEach {
                    expectThat(it.description).isEqualTo("hello")
                }
            }
        }
    }

    @Test
    fun `cursor fails with database exception`() {
        val sql = "asdfgadfgdafg"

        runTest {
            database.read {
                expectThrows<DatabaseException> { mapper().cursor<ClassWithStandardMappings>(sql, batchSize = 9).first() }
            }
        }
    }

    @Test
    fun `can extract db exception`() {
        runTest {
            database.read {
                try {
                    mapper().cursor<ClassWithStandardMappings>("asdfgadfgdafg").first()
                    fail()
                } catch (d: DatabaseException) {
                    if (DATABASE_TYPE == DatabaseType.POSTGRES) {
                        val ex = d.unwrapOrThrow<PgException>()
                        expectThat(ex.sqlState).isEqualTo("42601")
                    } else {
                        val ex = d.unwrapOrThrow<JdbcSQLSyntaxErrorException>()
                        expectThat(ex.sqlState).isEqualTo("42001")
                    }
                }
            }
        }
    }

    @Test
    fun `can execute batches`() {
        runTest {
            database.write {
                val results = mapper().executeBatch(
                    "insert into batch_job_instance (job_instance_id, version, job_name, job_key) values ($1, $2, $3, $4)",
                    listOf(listOf(1, 1, "bla", "blup1"), listOf(2, 1, "bla", "blup2"), listOf(3, 1, "bla", "blup3"), listOf(4, 1, "bla", "blup4"), listOf(5, 1, "bla", "blup5")),
                    2
                )
                expectThat(results).hasSize(3)
            }
            checkCountOfTable("batch_job_instance", 5)
        }
    }

    @Test
    fun `batches fail with database exception`() {
        runTest {
            expectThrows<DatabaseException> {
                database.write {
                    mapper().executeBatch(
                        "insert into asdasd (job_instance_id, version, job_name, job_key) values ($1, $2, $3, $4)",
                        listOf(listOf(1, 1, "bla", "blup1"), listOf(2, 1, "bla", "blup2"), listOf(3, 1, "bla", "blup3"), listOf(4, 1, "bla", "blup4"), listOf(5, 1, "bla", "blup5")),
                        2
                    )
                }
            }
        }
    }

    @Test
    fun `rollback works`() {
        runTest {
            expectThrows<DatabaseException> {
                database.write {
                    rollback()
                    expectThrows<DatabaseException> { rollback() }.get { message }.isEqualTo("rollback failed")
                }
            }.get { message }.isEqualTo("commit failed")
        }
    }

    @Test
    fun `can execute statement`() {
        runTest {
            database.write {
                val res = mapper().execute(
                    "insert into batch_job_instance (job_instance_id, version, job_name, job_key) values ($1, $2, $3, $4)",
                    1, 1, "bla", "blup1"
                )
                if (DATABASE_TYPE == DatabaseType.POSTGRES) {
                    expectThat(res.size()).isEqualTo(0)
                } else {
                    expectThat(res.size()).isEqualTo(-1)
                }
            }
            checkCountOfTable("batch_job_instance", 1)
        }
    }

    @Test
    fun `returns empty list when nothing is found`() {
        val sql = "select id, 'hello' as description, 8934578347548 as lockDate from resource_node where id=$1"

        runTest {
            createFiles(3)
            database.read {
                val listOfNodes = mapper().select<ClassWithStandardMappings>(sql, UUID.randomUUID())
                expectThat(listOfNodes).hasSize(0)
            }
        }
    }

    @Test
    fun `can select one entity`() {
        val sql = "select id, 'hello' as description, current_timestamp as lockDate from resource_node"

        runTest {
            createFiles(1)
            database.read {
                val node = mapper().selectOne<ClassWithStandardMappings>(sql)!!
                expectThat(node.description).isEqualTo("hello")
            }
        }
    }

    @Test
    fun `can map query with parameter`() {
        val sql = "select id, description, revision_time as lockDate from resource_node where id = $1"

        runTest {
            val fileId = createFiles(3).random().resource!!.id

            database.read {
                val listOfNodes = mapper().select<ClassWithStandardMappings>(sql, fileId)
                expectThat(listOfNodes).hasSize(1)
            }
        }
    }

    @Test
    fun `fails on nullable property with default`() {
        val sql = "select id from resource_node"
        runTest {
            database.read {
                expectThrows<DatabaseException> { mapper().select<ClassWithNullableAndDefault>(sql) }
            }
        }
    }

    @Test
    fun `fails on invalid sql`() {
        val sql = "asdfjghkadfhgksdfhjg"
        runTest {
            database.read {
                expectThrows<DatabaseException> { mapper().select<ClassWithStandardMappings>(sql) }
            }
        }
    }

    @Test
    fun `can map sealed class`() {
        val sql = "select id, 1 as sec1, 2 as sec2, 'FOV' as type from resource_node where id = $1"

        runTest {
            val fileId = createFiles(3).random().resource!!.id

            database.read {
                val node = mapper().select<ClassWithSealedClasses>(sql, fileId).single()
                expectThat(node.sec1).isEqualTo(SecurityConcept.Public)
                expectThat(node.sec2).isEqualTo(SecurityConcept.Space)
                expectThat(node.type).isEqualTo(ResourceType.FolderVersion)
            }
        }
    }

    @Test
    fun `populates default values`() {
        val sql = "select id, description from resource_node where id = $1"

        runTest {
            val fileId = createFiles(3).random().resource!!.id

            database.read {
                val node = mapper().select<ClassWithDefaults>(sql, fileId).single()
                expectThat(node.description).isEqualTo("hansi")
            }
        }
    }

    @Test
    fun `can map db primitive types`() {
        val sql = "select id from resource_node"

        runTest {
            createFiles(3)

            database.read {
                val listOfUUIDs = mapper().selectPrimitive<UUID>(sql)
                expectThat(listOfUUIDs).isA<List<UUID>>()
                expectThat(listOfUUIDs).hasSize(3)
            }
        }
    }

    @Test
    fun `can map value classes when selecting primitives`() {
        val sql = "select id from resource_node"

        runTest {
            createFiles(3)

            database.read {
                val listOfUUIDs = mapper().selectPrimitive<ResourceId>(sql)
                expectThat(listOfUUIDs).isA<List<UUID>>()
                expectThat(listOfUUIDs).isA<List<ResourceId>>()
                expectThat(listOfUUIDs).hasSize(3)
            }
        }
    }

    @Test
    fun `can map db boolean types`() {
        val sql = "select true from resource_node"

        runTest {
            createFiles(3)

            database.read {
                val listOfUUIDs = mapper().selectPrimitive<Boolean>(sql)
                expectThat(listOfUUIDs).isA<List<Boolean>>()
                expectThat(listOfUUIDs).hasSize(3)
            }
        }
    }

    @Test
    fun `can select one primitive`() {
        val sql = "select id from resource_node"

        runTest {
            createFiles(1)

            database.read {
                val uuid = mapper().selectOnePrimitive<UUID>(sql)!!
                expectThat(uuid).isA<UUID>()
            }
        }
    }

    @Test
    fun `returns empty list of nothing is found`() {
        val sql = "select id from resource_node where id = $1"

        runTest {
            createFiles(1)

            database.read {
                val uuids = mapper().selectPrimitive<UUID>(sql, UUID.randomUUID())
                expectThat(uuids).isEmpty()
            }
        }
    }

    @Test
    fun `fails if nothing is found`() {
        val sql = "select id from resource_node"

        runTest {
            database.read {
                expectThrows<DatabaseException> { mapper().selectOneExistingPrimitive<UUID>(sql) }
            }
        }
    }

    @Test
    fun `fails if too many rows are returned`() {
        val sql = "select id from resource_node"

        runTest {
            database.read {
                createFiles(2)
                expectThrows<DatabaseException> { mapper().selectOneExistingPrimitive<UUID>(sql) }
            }
        }
    }

    @Test
    fun `fails if more than one column is selected`() {
        val sql = "select id, description from resource_node"

        runTest {
            createFiles(1)

            database.read {
                expectThrows<DatabaseException> { mapper().selectPrimitive<UUID>(sql) }
            }
        }
    }

    @Test
    fun `can handle string enums`() {
        val sql = "select 'A' as enum1, 'B' as enum2 from resource_node"

        runTest {
            createFiles(3)

            database.read {
                val node = mapper().select<ClassWithEnums>(sql).random()
                expectThat(node.enum1).isEqualTo(TestEnum.A)
                expectThat(node.enum2).isEqualTo(TestEnum.B)
            }
        }
    }
}
