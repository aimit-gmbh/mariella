package org.mariella.test.example.implementation;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.mariella.persistence.runtime.PersistenceException;
import org.mariella.test.common.Mariella;
import org.mariella.test.common.MariellaUtil;
import org.mariella.test.example.api.CreateCompany;
import org.mariella.test.model.Company;
import org.mariella.test.model.EMail;
import org.mariella.test.model.Phone;

public class CompanyContext {
	public static <T> T withCompanyContext(Mariella mariella, WithContextCallback<T, CompanyContext> callback) {
		return withCompanyContext(mariella, null, callback);
	}
	public static <T> T withCompanyContext(Mariella mariella, UUID id, WithContextCallback<T, CompanyContext> callback) {
		return mariella.doInConnection(connection -> {
			try {
				CompanyContext context = new CompanyContext(new MariellaUtil(mariella, connection), id);
				T result = callback.withContext(context);
				context.mu.persist();
				return result;
			} catch(SQLException e) {
				throw new PersistenceException(e);
			}
		});
	}
	
	private final MariellaUtil mu;
	private CompanyCluster company;
	private UUID companyId;
	
public CompanyContext(MariellaUtil mu, UUID companyId) {
	this.mu = mu;
	company = new CompanyCluster(mu, companyId);
}

public UUID createCompany(CreateCompany create) {
	assertNull(company);
	assertNull(companyId);
	Company c = new Company();
	c.setId(UUID.randomUUID());
	mu.getModificationTracker().addNewParticipant(c);
	c.setAlias(create.alias);
	c.setName(create.name);
	company = new CompanyCluster(c);
	return c.getId();
}

public void lock() throws SQLException {
	String sql = "select id from company where id = ? for update";
	try(PreparedStatement ps = mu.getConnection().prepareStatement(sql)) {
		ps.setObject(1, company.getId());
		try(ResultSet rs = ps.executeQuery()) {
			rs.next();
		}
	}
}

public boolean contact()  {
	// first we try the call
	// phone numbers will be loaded on first call of company.get()
	company.withPhoneNumbers();
	for(Phone p : company.get().getPhoneNumbers()) {
		if(p.getPhoneNumber() != null) {
			// gotcha
			return true;
		}
	}
	// no contact yet, try to send an email
	company.withMailAddresses();
	for(EMail m : company.get().getMailAddresses()) {
		if(m.getMail() != null) {
			// this time
			return true;
		}
	}
	return false;
}

}
