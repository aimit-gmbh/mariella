package org.mariella.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mariella.persistence.database.StandardUUIDConverter;
import org.mariella.persistence.jdbc.JdbcQueryExecutor;
import org.mariella.persistence.persistor.BatchingPersistorStrategy;
import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.query.BinaryCondition;
import org.mariella.persistence.query.Literal;
import org.mariella.persistence.query.QueryBuilder;
import org.mariella.persistence.query.StringLiteral;
import org.mariella.persistence.query.TableReference;
import org.mariella.test.common.MariellaUtil;
import org.mariella.test.model.Company;
import org.mariella.test.model.Partner;
import org.mariella.test.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SimpleTest extends AbstractTest {
	protected static final Logger logger = LoggerFactory.getLogger(SimpleTest.class);
	
    @Test
    public void batchSqlError() throws Exception {
        createPerson("alias", "lastname1", "firstname1");
        createPerson("this alias is much too long", "lastname2", "firstname2");

        boolean error = false;
        try {
            mu.persist(new BatchingPersistorStrategy<>());
        } catch (RuntimeException e) {
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void batch() throws Exception {
        Person tina = createTina();
        Person wolfi = createWolfi();
        Person flani = createFlani();
        Person ingrid = createIngrid();

        mu.persist(new BatchingPersistorStrategy<>());

        UUID tinaId = tina.getId();
        UUID wolfiId = wolfi.getId();
        UUID flaniId = flani.getId();
        UUID ingridId = ingrid.getId();

        tina.setFirstName(tina.getFirstName() + "1");

        wolfi.setAlias(wolfi.getAlias() + "1");
        wolfi.setFirstName(wolfi.getFirstName() + "1");
        flani.setFirstName(flani.getFirstName() + "1");
        flani.setAlias(flani.getAlias() + "1");

        ingrid.setFirstName(ingrid.getFirstName() + "1");
        ingrid.setLastName(ingrid.getLastName() + "1");

        mu.persist(new BatchingPersistorStrategy<>());

        checkPerson(tinaId, "dr.", "Tina1", "Sulzenbacher");
        checkPerson(wolfiId, "wolfi1", "Wolfgang1", "Schwarzenbrunner");
        checkPerson(flaniId, "flani1", "Christian1", "Flandorfer");
        checkPerson(ingridId, "ingrid", "Ingrid1", "Wieser1");

        mu.getModificationTracker().remove(tina);
        mu.getModificationTracker().remove(wolfi);
        mu.getModificationTracker().remove(flani);

        mu.persist(new BatchingPersistorStrategy<>());
    }

    private void checkPerson(UUID id, String alias, String firstName, String lastName) {
    	// we want to load the into a different modificationtracker for checking, so
    	// the load can in no way interfere with existing state
    	MariellaUtil myMu = new MariellaUtil(mariella, mu.getConnection());

        Person p = myMu.loadById(Person.class, false, id, "root");
        assertNotNull(p);
        if (alias != null) {
            assertEquals(alias, p.getAlias());
        }
        if (firstName != null) {
            assertEquals(firstName, p.getFirstName());
        }
        if (lastName != null) {
            assertEquals(lastName, p.getLastName());
        }
    }


    @Test
    public void test() throws Exception {
        create();
        mu.resetModificationTracker();
        load();
    }

    private void create() throws Exception {
        logger.info("inserting");

        Person p = new Person();
        p.setId(UUID.randomUUID());
        mu.getModificationTracker().addNewParticipant(p);
        p.setAlias("hs");
        p.setFirstName("Hug");
        p.setLastName("Schlonz");

        p = new Person();
        p.setId(UUID.randomUUID());
        mu.getModificationTracker().addNewParticipant(p);
        p.setAlias("wolfi");
        p.setFirstName("Wolfgang");
        p.setLastName("Schwarzenbrunner");

        Company c = new Company();
        c.setId(UUID.randomUUID());
        mu.getModificationTracker().addNewParticipant(c);
        c.setAlias("Bellaflor");
        c.setName("Bellaflora Blumen GmbH & Co KG");

        p.getCollaborators().add(c);

        mu.persist();

        logger.info("updating");

        p.setFirstName("Hugo");
        c.setAlias("Bellaflora");
        mu.persist();
    }

    private void load() throws Exception {
        // Query Hugo by firstName
        // This query generates too many joins (company join is useless) -> SecondaryTableJoinBuilder
        logger.info("loading");
        QueryBuilder queryBuilder;
        JdbcQueryExecutor queryExecutor;

        queryBuilder = new QueryBuilder(mu.getMariella().getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.firstName"), new StringLiteral("Hugo")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, mu.createDatabaseAccess());
        UUID id = (UUID) queryExecutor.queryforObject();
        assertNotNull(id);


        // load cluster
        logger.info("loading cluster");
        ClusterDescription cd = new ClusterDescription(getClassDescription(Partner.class), "root.collaborators", "root");
        Person hugo = mu.loadById(cd, id, false);
        assertNotNull(hugo);
        assertEquals(hugo.getFirstName(), "Hugo");
        assertEquals(hugo.getCollaborators().size(), 1);
        Company c = (Company) hugo.getCollaborators().get(0);
        assertTrue(c.getName().startsWith("Bellaflora"));
    }


    // this fails because it is challenging to determine the alias of the discriminator´s TableReference
    @SuppressWarnings("unused")
    private void problem() throws Exception {
        logger.info("loading");
        QueryBuilder queryBuilder;
        JdbcQueryExecutor queryExecutor;

        queryBuilder = new QueryBuilder(mu.getMariella().getSchemaMapping());
        queryBuilder.join(getClassDescription(Partner.class), "root");
        queryBuilder.addSelectItem("root.id");
        queryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.firstName"), new StringLiteral("Hugo")));

        queryExecutor = new JdbcQueryExecutor(queryBuilder, mu.createDatabaseAccess());
        UUID id = (UUID) queryExecutor.queryforObject();
        assertNotNull(id);

        // query discrimiator of Hugo
        QueryBuilder discriminatorQueryBuilder = new QueryBuilder(mu.getMariella().getSchemaMapping());
        TableReference tr = discriminatorQueryBuilder.join(getClassDescription(Partner.class), "root");
        discriminatorQueryBuilder.addSelectItem(
                b -> b.append(tr.getAlias()).append(".TYPE")
        );
        discriminatorQueryBuilder.and(BinaryCondition.eq(queryBuilder.createColumnReference("root.id"),
                new Literal<>(StandardUUIDConverter.Singleton, id)));

        queryExecutor = new JdbcQueryExecutor(discriminatorQueryBuilder, mu.createDatabaseAccess());
        String discriminator = (String) queryExecutor.queryforObject();
        assertEquals(discriminator, "P");
    }

}
