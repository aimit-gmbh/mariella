package org.mariella.test;

import org.junit.jupiter.api.Test;
import org.mariella.persistence.jdbc.JdbcQueryExecutor;
import org.mariella.persistence.query.*;
import org.mariella.test.model.Company;
import org.mariella.test.model.Partner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QueryConditionsTest extends AbstractSimpleTest {

    @Test
    public void testIsNullCondition() throws Exception {
        createModificationTracker();

        createPerson("nullTest", "NullLast", "NullFirst");
        persist();

        // Query with IsNotNull
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(new IsNotNullCondition(queryBuilder.createColumnReference("root.alias")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertFalse(results.isEmpty());
    }

    @Test
    public void testIsNullConditionForNullValues() throws Exception {
        createModificationTracker();

        Company c = new Company();
        c.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(c);
        c.setAlias("noName");
        // name is left null

        persist();

        // Query with IsNull on name
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Company.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(new IsNullCondition(queryBuilder.createColumnReference("root.name")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertFalse(results.isEmpty());
    }

    @Test
    public void testInCondition() throws Exception {
        createModificationTracker();

        createPerson("in1", "InLast1", "InFirst1");
        createPerson("in2", "InLast2", "InFirst2");
        createPerson("in3", "InLast3", "InFirst3");

        persist();

        // Query with IN condition
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");

        List<Expression> inValues = Arrays.asList(
                new StringLiteral("in1"),
                new StringLiteral("in2")
        );
        queryBuilder.and(new InCondition(queryBuilder.createColumnReference("root.alias"), inValues));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());
    }

    @Test
    public void testBinaryConditions() throws Exception {
        createModificationTracker();

        createPerson("bin1", "BinaryLast", "AAA");
        createPerson("bin2", "BinaryLast", "BBB");
        createPerson("bin3", "BinaryLast", "CCC");

        persist();

        // Test greater than
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.gt(queryBuilder.createColumnReference("root.firstName"), new StringLiteral("AAA")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("BinaryLast")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());

        // Test less than
        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.lt(queryBuilder.createColumnReference("root.firstName"), new StringLiteral("CCC")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("BinaryLast")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());

        // Test greater than or equal
        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.gteq(queryBuilder.createColumnReference("root.firstName"), new StringLiteral("BBB")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("BinaryLast")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());

        // Test less than or equal
        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.lteq(queryBuilder.createColumnReference("root.firstName"), new StringLiteral("BBB")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("BinaryLast")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());
    }

    @Test
    public void testNotEqualCondition() throws Exception {
        createModificationTracker();

        createPerson("ne1", "NotEqualLast", "First1");
        createPerson("ne2", "NotEqualLast", "First2");

        persist();

        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.noteq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("ne1")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("NotEqualLast")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(1, results.size());
    }

    @Test
    public void testLikeCondition() throws Exception {
        createModificationTracker();

        createPerson("like1", "LikeLast", "LikeFirst1");
        createPerson("like2", "LikeLast", "LikeFirst2");
        createPerson("other", "OtherLast", "OtherFirst");

        persist();

        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.like(queryBuilder.createColumnReference("root.alias"), new StringLiteral("like%")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());
    }

    @Test
    public void testOrderByClause() throws Exception {
        createModificationTracker();

        createPerson("orderA", "OrderLast", "ZZZ");
        createPerson("orderB", "OrderLast", "AAA");
        createPerson("orderC", "OrderLast", "MMM");

        persist();

        // Test ascending order
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.firstName");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("OrderLast")));
        queryBuilder.addOrderBy(queryBuilder.createColumnReference("root.firstName"));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(3, results.size());
        assertEquals("AAA", results.get(0)[0]);
        assertEquals("MMM", results.get(1)[0]);
        assertEquals("ZZZ", results.get(2)[0]);

        // Test descending order
        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.firstName");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("OrderLast")));
        queryBuilder.addOrderBy(new Descending(queryBuilder.createColumnReference("root.firstName")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        results = queryExecutor.queryForObjects();
        assertEquals(3, results.size());
        assertEquals("ZZZ", results.get(0)[0]);
        assertEquals("MMM", results.get(1)[0]);
        assertEquals("AAA", results.get(2)[0]);
    }

    @Test
    public void testBracketsCondition() throws Exception {
        createModificationTracker();

        createPerson("brack1", "BrackLast1", "First1");
        createPerson("brack2", "BrackLast2", "First2");
        createPerson("brack3", "BrackLast1", "First3");

        persist();

        // Test OR condition with brackets
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");

        Expression or = new Brackets(
                BinaryCondition.or(
                        BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("brack1")),
                        BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("brack2"))
                )
        );
        queryBuilder.and(or);

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(2, results.size());
    }

    @Test
    public void testCountQuery() throws Exception {
        createModificationTracker();

        createPerson("count1", "CountLast", "First1");
        createPerson("count2", "CountLast", "First2");
        createPerson("count3", "CountLast", "First3");

        persist();

        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new Count(queryBuilder.createColumnReference("root.id")));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.lastName"), new StringLiteral("CountLast")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        Object count = queryExecutor.queryforObject();
        assertNotNull(count);
        assertTrue(((Number) count).intValue() >= 3);
    }

    @Test
    public void testLiteralTypes() throws Exception {
        createModificationTracker();

        createPerson("lit", "LiteralLast", "LiteralFirst");
        persist();

        // Test IntegerLiteral
        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new IntegerLiteral(42));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("lit")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        Object result = queryExecutor.queryforObject();
        assertEquals(42, ((Number) result).intValue());

        // Test LongLiteral
        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new LongLiteral(123456789L));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("lit")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        result = queryExecutor.queryforObject();
        assertEquals(123456789L, ((Number) result).longValue());

        // Test DoubleLiteral
        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem(new DoubleLiteral(3.14));
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("lit")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        result = queryExecutor.queryforObject();
        assertEquals(3.14, ((Number) result).doubleValue(), 0.01);
    }

    @Test
    public void testMultipleSelectItems() throws Exception {
        createModificationTracker();

        createPerson("multi", "MultiLast", "MultiFirst");
        persist();

        QueryBuilder queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.addSelectItem("root.alias");
        queryBuilder.addSelectItem("root.firstName");
        queryBuilder.addSelectItem("root.lastName");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.alias"), new StringLiteral("multi")));

        JdbcQueryExecutor queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        List<Object[]> results = queryExecutor.queryForObjects();
        assertEquals(1, results.size());
        Object[] row = results.getFirst();
        assertEquals(4, row.length);
        assertNotNull(row[0]); // id
        assertEquals("multi", row[1]); // alias
        assertEquals("MultiFirst", row[2]); // firstName
        assertEquals("MultiLast", row[3]); // lastName
    }
}
