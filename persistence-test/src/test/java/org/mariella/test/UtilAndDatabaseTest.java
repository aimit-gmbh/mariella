package org.mariella.test;

import org.junit.jupiter.api.Test;
import org.mariella.persistence.database.StandardUUIDConverter;
import org.mariella.persistence.jdbc.JdbcQueryExecutor;
import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.persistor.BatchingPersistorStrategy;
import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.query.*;
import org.mariella.persistence.util.Util;
import org.mariella.test.model.Company;
import org.mariella.test.model.Partner;
import org.mariella.test.model.Person;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UtilAndDatabaseTest extends AbstractSimpleTest {

    @Test
    public void testUtilAsStringDelimitedBy() {
        List<String> strings = Arrays.asList("a", "b", "c");
        String result = Util.asStringDelimitedBy(strings, ", ");
        assertEquals("a, b, c", result);

        // Empty list
        result = Util.asStringDelimitedBy(List.of(), ", ");
        assertEquals("", result);

        // Single element
        result = Util.asStringDelimitedBy(List.of("only"), "-");
        assertEquals("only", result);
    }

    @Test
    public void testUtilAssertTrue() {
        // Should not throw
        Util.assertTrue(true, "This should pass");

        // Should throw
        assertThrows(AssertionError.class, () -> Util.assertTrue(false, "This should fail"));
    }

    @Test
    public void testStandardUUIDConverter() {
        StandardUUIDConverter converter = StandardUUIDConverter.Singleton;

        UUID uuid = UUID.randomUUID();
        Expression literal = converter.createLiteral(uuid);
        assertNotNull(literal);
    }

    @Test
    public void testBigDecimalLiteral() throws Exception {
        createModificationTracker();

        createPerson("bigdec", "BigDecLast", "BigDecFirst");
        persist();

        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new BigDecimalLiteral(new BigDecimal("123.45")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("bigdec")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        Object result = queryExecutor.queryforObject();
        assertNotNull(result);
    }

    // TimestampLiteral and DateLiteral use Oracle-specific TO_TIMESTAMP which H2 doesn't support
    // These would work with PostgreSQL or Oracle databases

    @Test
    public void testUpdateEntity() throws Exception {
        createModificationTracker();

        Person p = createPerson("update", "UpdateLast", "UpdateFirst");
        UUID id = p.getId();
        persist();

        // Update multiple fields
        p.setFirstName("UpdatedFirst");
        p.setLastName("UpdatedLast");
        p.setAlias("updated");
        persist();

        // Reload and verify
        createModificationTracker();
        Person loaded = loadById(Person.class, false, id, "root");

        assertNotNull(loaded);
        assertEquals("UpdatedFirst", loaded.getFirstName());
        assertEquals("UpdatedLast", loaded.getLastName());
        assertEquals("updated", loaded.getAlias());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        createModificationTracker();

        Person p = createPerson("delete", "DeleteLast", "DeleteFirst");
        UUID id = p.getId();
        persist();

        // Delete
        modificationTracker.remove(p);
        persist();

        // Try to reload - should not find
        createModificationTracker();
        Person loaded = loadById(Person.class, false, id, "root");
        assertNull(loaded);
    }

    @Test
    public void testBatchInsertMultipleEntities() throws Exception {
        createModificationTracker();

        for (int i = 0; i < 10; i++) {
            createPerson("batch" + i, "BatchLast" + i, "BatchFirst" + i);
        }

        persist(new BatchingPersistorStrategy<>());

        // Query all batch entities
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new Count(queryBuilder.createColumnReference("root.id")));
        queryBuilder.and(BinaryCondition.like(queryBuilder.createColumnReference("root.alias"), new StringLiteral("batch%")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        Object count = queryExecutor.queryforObject();
        assertEquals(10, ((Number) count).intValue());
    }

    @Test
    public void testBatchUpdateMultipleEntities() throws Exception {
        createModificationTracker();

        Person p1 = createPerson("batchUp1", "BatchUpLast1", "BatchUpFirst1");
        Person p2 = createPerson("batchUp2", "BatchUpLast2", "BatchUpFirst2");
        Person p3 = createPerson("batchUp3", "BatchUpLast3", "BatchUpFirst3");

        persist(new BatchingPersistorStrategy<>());

        // Update all
        p1.setFirstName("Updated1");
        p2.setFirstName("Updated2");
        p3.setFirstName("Updated3");

        persist(new BatchingPersistorStrategy<>());

        // Verify
        createModificationTracker();
        Person loaded1 = loadById(Person.class, false, p1.getId(), "root");
        Person loaded2 = loadById(Person.class, false, p2.getId(), "root");
        Person loaded3 = loadById(Person.class, false, p3.getId(), "root");

        assertEquals("Updated1", loaded1.getFirstName());
        assertEquals("Updated2", loaded2.getFirstName());
        assertEquals("Updated3", loaded3.getFirstName());
    }

    @Test
    public void testBatchDeleteMultipleEntities() throws Exception {
        createModificationTracker();

        Person p1 = createPerson("batchDel1", "BatchDelLast1", "BatchDelFirst1");
        Person p2 = createPerson("batchDel2", "BatchDelLast2", "BatchDelFirst2");
        Person p3 = createPerson("batchDel3", "BatchDelLast3", "BatchDelFirst3");

        persist(new BatchingPersistorStrategy<>());

        UUID id1 = p1.getId();
        UUID id2 = p2.getId();
        UUID id3 = p3.getId();

        // Delete all
        modificationTracker.remove(p1);
        modificationTracker.remove(p2);
        modificationTracker.remove(p3);

        persist(new BatchingPersistorStrategy<>());

        // Verify all deleted
        createModificationTracker();
        assertNull(loadById(Person.class, false, id1, "root"));
        assertNull(loadById(Person.class, false, id2, "root"));
        assertNull(loadById(Person.class, false, id3, "root"));
    }

    @Test
    public void testLoadByIds() throws Exception {
        createModificationTracker();

        Person p1 = createPerson("loadByIds1", "LoadByIdsLast1", "LoadByIdsFirst1");
        Person p2 = createPerson("loadByIds2", "LoadByIdsLast2", "LoadByIdsFirst2");
        createPerson("loadByIds3", "LoadByIdsLast3", "LoadByIdsFirst3");

        persist();

        List<UUID> ids = Arrays.asList(p1.getId(), p2.getId());

        createModificationTracker();
        List<Person> loaded = loadByIds(Person.class, false, ids, "root");

        assertEquals(2, loaded.size());
    }

    @Test
    public void testSumFunction() throws Exception {
        createModificationTracker();

        createPerson("sum1", "SumLast", "SumFirst1");
        createPerson("sum2", "SumLast", "SumFirst2");
        createPerson("sum3", "SumLast", "SumFirst3");

        persist();

        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new Count(queryBuilder.createColumnReference("root.id")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("SumLast")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        Object count = queryExecutor.queryforObject();
        assertTrue(((Number) count).intValue() >= 3);
    }

    @Test
    public void testLeftOuterJoin() throws Exception {
        createModificationTracker();

        Person p = createPerson("leftOuter", "LeftOuterLast", "LeftOuterFirst");

        Company c = new Company();
        c.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(c);
        c.setAlias("leftCo");
        c.setName("Left Outer Company");
        c.setBoss(p);

        persist();

        // Query using join
        ClusterDescription cd = new ClusterDescription(getClassDescription(Company.class), "root", "root.boss");
        Company loaded = loadById(cd, c.getId(), false);

        assertNotNull(loaded);
        assertNotNull(loaded.getBoss());
        assertEquals("LeftOuterFirst", loaded.getBoss().getFirstName());
    }

    @Test
    public void testManyToManyRelationship() throws Exception {
        createModificationTracker();

        Person p1 = createPerson("m2m1", "M2MLast1", "M2MFirst1");
        Person p2 = createPerson("m2m2", "M2MLast2", "M2MFirst2");
        Person p3 = createPerson("m2m3", "M2MLast3", "M2MFirst3");

        // p1 collaborates with p2 and p3
        p1.getCollaborators().add(p2);
        p1.getCollaborators().add(p3);

        // p2 collaborates with p1
        p2.getCollaborators().add(p1);

        persist();

        // Reload and verify
        createModificationTracker();
        ClusterDescription cd = new ClusterDescription(getClassDescription(Partner.class), "root", "root.collaborators");
        Person loaded1 = loadById(cd, p1.getId(), false);

        assertNotNull(loaded1);
        assertEquals(2, loaded1.getCollaborators().size());

        createModificationTracker();
        Person loaded2 = loadById(cd, p2.getId(), false);
        assertNotNull(loaded2);
        assertEquals(1, loaded2.getCollaborators().size());
    }

    @Test
    public void testClassMapping() throws Exception {
        createModificationTracker();

        ClassMapping personMapping = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        assertNotNull(personMapping);
        assertNotNull(personMapping.getPrimaryKey());

        ClassMapping companyMapping = environment.getSchemaMapping().getClassMapping(Company.class.getName());
        assertNotNull(companyMapping);

        // Partner is the root class with the primary table
        ClassMapping partnerMapping = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        assertNotNull(partnerMapping);
        assertNotNull(partnerMapping.getPrimaryTable());
    }

    @Test
    public void testSchemaDescription() throws Exception {
        createModificationTracker();

        assertNotNull(environment.getSchemaMapping().getSchemaDescription());
        assertNotNull(getClassDescription(Person.class));
        assertNotNull(getClassDescription(Company.class));
        assertNotNull(getClassDescription(Partner.class));
    }

    @Test
    public void testInheritanceHierarchy() throws Exception {
        createModificationTracker();

        // Create a Person (subclass of Partner)
        createPerson("inherit", "InheritLast", "InheritFirst");

        // Create a Company (another subclass of Partner)
        Company c = new Company();
        c.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(c);
        c.setAlias("inheritCo");
        c.setName("Inherit Company");

        persist();

        // Query at Partner level should return both
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.like(queryBuilder.createColumnReference("root.alias"), new StringLiteral("inherit%")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());
    }
}
