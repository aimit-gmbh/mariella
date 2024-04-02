package org.mariella.test;

import org.junit.jupiter.api.Test;
import org.mariella.persistence.database.StandardUUIDConverter;
import org.mariella.persistence.jdbc.JdbcQueryExecutor;
import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.query.*;
import org.mariella.test.model.File;
import org.mariella.test.model.Folder;
import org.mariella.test.model.Resource;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest extends AbstractSimpleTest {

    @Test
    public void test() throws Exception {
        create();
        load();
        queryDiscriminator();
    }

    private void create() throws Exception {
        logger.info("inserting");

        createModificationTracker();
        Folder folder = new Folder();
        folder.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(folder);
        folder.setName("root");
        folder.setLastModified(new Timestamp(System.currentTimeMillis()));

        File file = new File();
        file.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(file);
        file.setParent(folder);
        assertEquals(folder.getChildren().size(), 1);
        assertSame(folder.getChildren().get(0), file);
        file.setName("test.txt");
        file.setLastModified(new Timestamp(System.currentTimeMillis()));
        file.setSize(5);

        persist();

        logger.info("updating");

        file.setSize(7);
        persist();
    }

    private void load() throws Exception {
        logger.info("loading");
        QueryBuilder queryBuilder;
        JdbcQueryExecutor queryExecutor;

        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Resource.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.name"), new StringLiteral("test.txt")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        UUID id = (UUID) queryExecutor.queryforObject();
        assertNotNull(id);


        ClusterDescription cd;
        Resource r;
        File file;

        // load cluster
        logger.info("loading cluster");
        createModificationTracker();
        cd = new ClusterDescription(getClassDescription(Resource.class), "root", "root.parent");
        r = loadById(cd, id, false);
        assertInstanceOf(File.class, r);
        file = (File) r;
        assertEquals(file.getSize(), 7);
        assertNotNull(file.getParent());
        assertTrue(file.getParent().getChildren().isEmpty());

        // load cluster
        logger.info("loading cluster");
        createModificationTracker();
        cd = new ClusterDescription(getClassDescription(Resource.class), "root", "root.parent", "root.parent.children");
        r = loadById(cd, id, false);
        assertInstanceOf(File.class, r);
        file = (File) r;
        assertEquals(file.getSize(), 7);
        assertNotNull(file.getParent());
        assertFalse(file.getParent().getChildren().isEmpty());
    }


    private void queryDiscriminator() throws Exception {
        logger.info("query descriminator");
        QueryBuilder queryBuilder;
        JdbcQueryExecutor queryExecutor;

        queryBuilder = new QueryBuilder(environment.getSchemaMapping());
        queryBuilder.join(getClassDescription(Resource.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.name"), new StringLiteral("test.txt")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, createDatabaseAccess());
        UUID id = (UUID) queryExecutor.queryforObject();
        assertNotNull(id);

        QueryBuilder discriminatorQueryBuilder = new QueryBuilder(environment.getSchemaMapping());
        TableReference tr = discriminatorQueryBuilder.join(getClassDescription(Resource.class), "root");
        discriminatorQueryBuilder.addSelectItem(
                b -> b.append(tr.getAlias()).append(".TYPE")
        );
        discriminatorQueryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.id"),
                new Literal<>(StandardUUIDConverter.Singleton, id)));

        queryExecutor = new JdbcQueryExecutor(discriminatorQueryBuilder, createDatabaseAccess());
        String discriminator = (String) queryExecutor.queryforObject();
        assertEquals(discriminator, "File");
    }

}
