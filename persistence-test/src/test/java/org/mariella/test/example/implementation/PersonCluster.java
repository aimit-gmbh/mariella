package org.mariella.test.example.implementation;

import java.util.UUID;

import org.mariella.test.common.MariellaUtil;
import org.mariella.test.model.Company;
import org.mariella.test.model.Partner;
import org.mariella.test.model.Person;

public class PersonCluster extends PartnerCluster<Partner> {
	
public PersonCluster(MariellaUtil mariella, UUID id) {
	super(new ExistingCluster<>(mariella, Company.class, id));
}

public PersonCluster(Company company) {
	super(new LoadedCluster<>(company.getId(), company));
}

@Override
protected Class<?> getEntityClass() {
	return Person.class;
}

}
