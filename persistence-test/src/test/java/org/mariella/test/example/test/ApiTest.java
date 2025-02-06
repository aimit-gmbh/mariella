package org.mariella.test.example.test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mariella.test.common.Mariella;
import org.mariella.test.example.api.Api;
import org.mariella.test.example.api.CreateCompany;
import org.mariella.test.example.implementation.ApiImpl;

public class ApiTest {
	public final static String PERSISTENCE_UNIT_NAME = "sample/h2";
	
	private Mariella mariella;
	private Api api;
	
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
   		api = new ApiImpl(mariella);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        mariella.destroy();
    }
    
	@Test
	public void contact() {
		CreateCompany create = new CreateCompany();
		create.alias = "alias";
		create.name = "name";
		create.phone = "12345";
		UUID id = api.createCompany(create);
		api.contactCompany(id);
	}
}
