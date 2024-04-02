package org.mariella.test.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "RESOURCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Folder")
public class Folder extends Resource {

}
