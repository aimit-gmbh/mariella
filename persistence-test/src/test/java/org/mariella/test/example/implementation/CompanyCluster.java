package org.mariella.test.example.implementation;

import java.util.UUID;

import org.mariella.test.common.ClusterBuilder;
import org.mariella.test.common.MariellaUtil;
import org.mariella.test.model.Company;
import org.mariella.test.model.Person;

public class CompanyCluster extends PartnerCluster<Company> {
	public static final ClusterBuilder cbWithEmploymentsAndFlatPartners = new ClusterBuilder(Company.class)
			.addPathExpressions(
				"employments",
				"employment.partner"
			);

	public static final ClusterBuilder cbWithEmploymentsAndFullPartners = new ClusterBuilder(Company.class)
			.addPathExpressions(
				"employments",
				"employment.partner")
			.addPathExpressions("employments.partner", PartnerCluster.cbFull(Person.class));

	// same as cbWithEmploymentsAndFullPartners
	public static final ClusterBuilder cbWithEmploymentsAndFullPartners2 = new ClusterBuilder(Company.class)
			.addPathExpressions(
				"employments",
				"employment.partner")
			.addPathExpressions("employments.partner", PartnerCluster.cbWithMailAddresses(Person.class))
			.addPathExpressions("employments.partner", PartnerCluster.cbWithPhoneNumbers(Person.class));

	public static final ClusterBuilder cbFull = new ClusterBuilder(Company.class)
			.addPathExpressions(PartnerCluster.cbFull(Company.class))
			.addPathExpressions(cbWithEmploymentsAndFullPartners);

public CompanyCluster(MariellaUtil mariella, UUID id) {
	super(new ExistingCluster<>(mariella, Company.class, id));
}

public CompanyCluster(Company company) {
	super(new LoadedCluster<>(company.getId(), company));
}

@Override
protected Class<?> getEntityClass() {
	return Company.class;
}

public CompanyCluster withEmploymentsAndFlatPartners() {
	required(cbWithEmploymentsAndFlatPartners);
	return this;
}

public CompanyCluster withEmploymentsAndFullPartners() {
	required(cbWithEmploymentsAndFullPartners);
	return this;
}

public CompanyCluster full() {
	required(cbFull);
	return this;
}

}
