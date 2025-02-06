package org.mariella.test.example.implementation;

import java.util.UUID;

import org.mariella.test.common.Mariella;
import org.mariella.test.example.api.Api;
import org.mariella.test.example.api.CreateCompany;

public class ApiImpl implements Api {
	private final Mariella mariella;
	
public ApiImpl(Mariella mariella) {
	this.mariella = mariella;
}
	
@Override
public UUID createCompany(CreateCompany create) {
	return CompanyContext.withCompanyContext(mariella, context -> {
		return context.createCompany(create);
	});
}

@Override
public boolean contactCompany(UUID id) {
	return CompanyContext.withCompanyContext(mariella, id, context -> {
		context.lock();
		return context.contact();
	});
}

}
