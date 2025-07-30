package org.mariella.test.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@jakarta.persistence.Entity
@Table(name = "RESOURCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Folder")
public class Folder extends Resource {

}
