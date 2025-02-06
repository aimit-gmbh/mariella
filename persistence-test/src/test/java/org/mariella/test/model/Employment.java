package org.mariella.test.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name="EMPLOYMENT")
public class Employment extends Entity {
	private Company employer;
	private Person employee;
	private int employmentYear;
	
@OneToOne
@JoinColumn(name = "employee_id", referencedColumnName = "id")
public Person getEmployee() {
	return employee;
}

public void setEmployee(Person employee) {
	propertyChangeSupport.firePropertyChange("employee", this.employee, this.employee = employee);
}

@OneToOne
@JoinColumn(name = "employer_id", referencedColumnName = "id")
public Company getEmployer() {
	return employer;
}

public void setEmployer(Company employer) {
	propertyChangeSupport.firePropertyChange("employer", this.employer, this.employer = employer);
}

@Column(name="EMPLOYMENT_YEAR")
public int getEmploymentYear() {
	return employmentYear;
}

public void setEmploymentYear(int employmentYear) {
	propertyChangeSupport.firePropertyChange("employmentYear", this.employmentYear, this.employmentYear = employmentYear);
}

}
