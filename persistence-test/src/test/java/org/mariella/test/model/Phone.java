package org.mariella.test.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name="phone")
public class Phone extends Entity {
	private Partner partner;
	private String phoneNumber;
	
	@ManyToOne
	@JoinColumn(name = "partner_id", referencedColumnName = "id")
	public Partner getPartner() {
		return partner;
	}
	
	public void setPartner(Partner partner) {
		propertyChangeSupport.firePropertyChange("partner", this.partner, this.partner = partner);
	}
	
	@Column(name="phone_number")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		propertyChangeSupport.firePropertyChange("mail", this.phoneNumber, this.phoneNumber = phoneNumber);
	}
	
}
