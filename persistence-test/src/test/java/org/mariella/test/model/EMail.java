package org.mariella.test.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name="email")
public class EMail extends Entity {
	private Partner partner;
	private String mail;
	
	@ManyToOne
	@JoinColumn(name = "partner_id", referencedColumnName = "id")
	public Partner getPartner() {
		return partner;
	}
	
	public void setPartner(Partner partner) {
		propertyChangeSupport.firePropertyChange("partner", this.partner, this.partner = partner);
	}
	
	@Column(name="mail")
	public String getMail() {
		return mail;
	}
	
	public void setMail(String mail) {
		propertyChangeSupport.firePropertyChange("mail", this.mail, this.mail = mail);
	}
	
}
