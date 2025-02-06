package org.mariella.test.example.api;

import java.util.UUID;

public interface Api {

	UUID createCompany(CreateCompany create);

	boolean contactCompany(UUID id);

}