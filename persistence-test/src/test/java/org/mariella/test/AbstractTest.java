package org.mariella.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.test.common.Mariella;
import org.mariella.test.common.MariellaUtil;
import org.mariella.test.model.Person;

public class AbstractTest {
    public final static String PERSISTENCE_UNIT_NAME = "sample/h2";
    
    protected Mariella mariella;
    protected MariellaUtil mu;
    protected Connection connection;

    @BeforeAll
    public static void beforeAll() throws SQLException {
    	DriverManager.registerDriver(new Driver());
    }
    
    @BeforeEach
    public void beforeEach() throws Exception {
    	BasicDataSource ds = new BasicDataSource();
    	ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    	ds.setUsername("sa");
    	ds.setPassword("");
    	
        mariella = new Mariella(PERSISTENCE_UNIT_NAME, "classpath:/db", ds);
   		mariella.migrateDatabase();
   		mariella.setupPersistence();
   		connection = ds.getConnection();
   		mu = new MariellaUtil(mariella, connection);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        connection.close();
        mariella.destroy();
    }

    protected Person createIngrid() {
        return createPerson("ingrid", "Wieser", "Ingrid");
    }

    protected Person createFlani() {
        return createPerson("flani", "Flandorfer", "Christian");
    }

    protected Person createWolfi() {
        return createPerson("wolfi", "Schwarzenbrunner", "Wolfgang");
    }

    protected Person createTina() {
        return createPerson("dr.", "Sulzenbacher", "Tina");
    }

    protected Person createPerson(String alias, String lastName, String firstName) {
        Person p = new Person();
        p.setId(UUID.randomUUID());
        mu.getModificationTracker().addNewParticipant(p);
        p.setAlias(alias);
        p.setFirstName(firstName);
        p.setLastName(lastName);
        return p;
    }

    protected ClassDescription getClassDescription(Class<?> clazz) {
        return mariella.getSchemaMapping().getSchemaDescription().getClassDescription(clazz.getName());
    }

}
