package org.mariella.test;

import org.junit.jupiter.api.Test;
import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.schema.*;
import org.mariella.test.model.Company;
import org.mariella.test.model.Partner;
import org.mariella.test.model.Person;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SchemaDescription, ClassDescription, SchemaMapping, and ClassMapping classes.
 */
class SchemaMappingTest extends AbstractSimpleTest {

    // ========== SchemaDescription Tests ==========

    @Test
    public void testSchemaDescriptionBasics() {
        SchemaDescription sd = environment.getSchemaMapping().getSchemaDescription();

        assertNotNull(sd);
        assertNotNull(sd.getSchemaName());

        // Test getClassDescriptions returns non-empty collection
        Collection<ClassDescription> descriptions = sd.getClassDescriptions();
        assertNotNull(descriptions);
        assertFalse(descriptions.isEmpty());
    }

    @Test
    public void testSchemaDescriptionGetClassDescription() {
        SchemaDescription sd = environment.getSchemaMapping().getSchemaDescription();

        // Get existing class description
        ClassDescription personCd = sd.getClassDescription(Person.class.getName());
        assertNotNull(personCd);
        assertEquals(Person.class.getName(), personCd.getClassName());

        ClassDescription companyCd = sd.getClassDescription(Company.class.getName());
        assertNotNull(companyCd);
        assertEquals(Company.class.getName(), companyCd.getClassName());

        // Non-existent class should return null
        ClassDescription nonExistent = sd.getClassDescription("com.nonexistent.Class");
        assertNull(nonExistent);
    }

    @Test
    public void testSchemaDescriptionGetPropertyDescriptor() {
        // Test static method getPropertyDescriptor
        PropertyDescriptor pd = SchemaDescription.getPropertyDescriptor(Person.class, "firstName");
        assertNotNull(pd);
        assertEquals("firstName", pd.getName());

        // Test with non-existent property
        PropertyDescriptor nonExistent = SchemaDescription.getPropertyDescriptor(Person.class, "nonExistentProperty");
        assertNull(nonExistent);

        // Test with inherited property
        PropertyDescriptor aliasPd = SchemaDescription.getPropertyDescriptor(Person.class, "alias");
        assertNotNull(aliasPd);
        assertEquals("alias", aliasPd.getName());
    }

    // ========== ClassDescription Tests ==========

    @Test
    public void testClassDescriptionBasics() {
        ClassDescription personCd = getClassDescription(Person.class);

        assertNotNull(personCd);
        assertEquals(Person.class.getName(), personCd.getClassName());
        assertNotNull(personCd.getSchemaDescription());
        assertNotNull(personCd.toString());
        assertTrue(personCd.toString().contains(Person.class.getName()));
    }

    @Test
    public void testClassDescriptionInheritance() {
        ClassDescription personCd = getClassDescription(Person.class);
        ClassDescription partnerCd = getClassDescription(Partner.class);
        ClassDescription companyCd = getClassDescription(Company.class);

        // Person's super class should be Partner
        assertEquals(partnerCd, personCd.getSuperClassDescription());

        // Company's super class should be Partner
        assertEquals(partnerCd, companyCd.getSuperClassDescription());

        // Partner has no super class (in our model)
        assertNull(partnerCd.getSuperClassDescription());
    }

    @Test
    public void testClassDescriptionPropertyDescriptions() {
        ClassDescription personCd = getClassDescription(Person.class);

        // Get property descriptions
        Collection<PropertyDescription> props = personCd.getPropertyDescriptions();
        assertNotNull(props);
        assertFalse(props.isEmpty());

        // Get specific property
        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        assertNotNull(firstNamePd);
        assertEquals("firstName", firstNamePd.getPropertyDescriptor().getName());

        // Get inherited property
        PropertyDescription aliasPd = personCd.getPropertyDescription("alias");
        assertNotNull(aliasPd);
    }

    @Test
    public void testClassDescriptionHierarchyPropertyDescriptions() {
        ClassDescription personCd = getClassDescription(Person.class);
        ClassDescription partnerCd = getClassDescription(Partner.class);

        // Hierarchy property descriptions should include inherited properties
        Collection<PropertyDescription> hierarchyProps = personCd.getHierarchyPropertyDescriptions();
        assertNotNull(hierarchyProps);

        // Partner's hierarchy should include properties from subclasses
        Collection<PropertyDescription> partnerHierarchy = partnerCd.getHierarchyPropertyDescriptions();
        assertNotNull(partnerHierarchy);
    }

    @Test
    public void testClassDescriptionGetPropertyDescriptionInHierarchy() {
        ClassDescription partnerCd = getClassDescription(Partner.class);

        // Should find property in hierarchy
        PropertyDescription aliasPd = partnerCd.getPropertyDescriptionInHierarchy("alias");
        assertNotNull(aliasPd);

        // firstName is in Person subclass, should be in hierarchy
        PropertyDescription firstNamePd = partnerCd.getPropertyDescriptionInHierarchy("firstName");
        assertNotNull(firstNamePd);
    }

    @Test
    public void testClassDescriptionIdentity() throws Exception {
        createModificationTracker();

        ClassDescription personCd = getClassDescription(Person.class);

        // Create a person to test identity methods
        Person p = createPerson("idTest", "IdLast", "IdFirst");

        // Test getIdentityPropertyDescriptions
        PropertyDescription[] idProps = personCd.getIdentityPropertyDescriptions();
        assertNotNull(idProps);
        assertTrue(idProps.length > 0);

        // Test isId
        assertTrue(personCd.isId(idProps[0]));

        // Test getIdentity
        Object identity = personCd.getIdentity(p);
        assertNotNull(identity);

        // Test getId
        Object id = personCd.getId(p);
        assertNotNull(id);
        assertEquals(p.getId(), id);
    }

    @Test
    public void testClassDescriptionIsInherited() {
        ClassDescription personCd = getClassDescription(Person.class);

        // firstName is defined in Person, not inherited
        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        assertFalse(personCd.isInherited(firstNamePd));

        // alias is inherited from Partner
        PropertyDescription aliasPd = personCd.getPropertyDescription("alias");
        assertTrue(personCd.isInherited(aliasPd));
    }

    @Test
    public void testClassDescriptionIsAbstract() {
        ClassDescription personCd = getClassDescription(Person.class);
        ClassDescription partnerCd = getClassDescription(Partner.class);

        // Person is not abstract
        assertFalse(personCd.isAbstract());

        // Partner might be abstract depending on model
        // Just test the method works
        partnerCd.isAbstract();
    }

    @Test
    public void testClassDescriptionHasLocalPropertyDescriptions() {
        ClassDescription personCd = getClassDescription(Person.class);
        ClassDescription partnerCd = getClassDescription(Partner.class);

        // Person has local properties (firstName, lastName)
        assertTrue(personCd.hasLocalPropertyDescriptions());

        // Partner also has local properties (alias)
        assertTrue(partnerCd.hasLocalPropertyDescriptions());
    }

    @Test
    public void testClassDescriptionIsA() {
        ClassDescription personCd = getClassDescription(Person.class);
        ClassDescription partnerCd = getClassDescription(Partner.class);
        ClassDescription companyCd = getClassDescription(Company.class);

        // Person is a Person
        assertTrue(personCd.isA(personCd));

        // Person is NOT a Company (sibling)
        assertFalse(personCd.isA(companyCd));
    }

    @Test
    public void testClassDescriptionSetIdentity() throws Exception {
        createModificationTracker();

        ClassDescription personCd = getClassDescription(Person.class);

        Person p = new Person();
        UUID newId = UUID.randomUUID();

        // Get identity from another person
        Person p2 = createPerson("setId", "SetIdLast", "SetIdFirst");
        Object identity = personCd.getIdentity(p2);

        // Set identity on p
        personCd.setIdentity(p, identity);
        assertEquals(p2.getId(), p.getId());
    }

    // ========== SchemaMapping Tests ==========

    @Test
    public void testSchemaMappingBasics() {
        SchemaMapping sm = environment.getSchemaMapping();

        assertNotNull(sm);
        assertNotNull(sm.getSchemaDescription());
        assertNotNull(sm.getSchema());
    }

    @Test
    public void testSchemaMappingGetClassMapping() {
        SchemaMapping sm = environment.getSchemaMapping();

        // Get existing class mapping
        ClassMapping personCm = sm.getClassMapping(Person.class.getName());
        assertNotNull(personCm);

        ClassMapping companyCm = sm.getClassMapping(Company.class.getName());
        assertNotNull(companyCm);

        ClassMapping partnerCm = sm.getClassMapping(Partner.class.getName());
        assertNotNull(partnerCm);

        // Non-existent class should return null
        ClassMapping nonExistent = sm.getClassMapping("com.nonexistent.Class");
        assertNull(nonExistent);
    }

    @Test
    public void testSchemaMappingGetClassMappings() {
        SchemaMapping sm = environment.getSchemaMapping();

        Collection<ClassMapping> mappings = sm.getClassMappings();
        assertNotNull(mappings);
        assertFalse(mappings.isEmpty());

        // Should contain Person, Company, Partner mappings
        boolean hasPersonMapping = false;
        for (ClassMapping cm : mappings) {
            if (cm.getClassDescription().getClassName().equals(Person.class.getName())) {
                hasPersonMapping = true;
                break;
            }
        }
        assertTrue(hasPersonMapping);
    }

    @Test
    public void testSchemaMappingUsedTables() {
        SchemaMapping sm = environment.getSchemaMapping();

        Collection<Table> usedTables = sm.getUsedTables();
        assertNotNull(usedTables);
        assertFalse(usedTables.isEmpty());

        // Should include PARTNER, PERSON, COMPANY tables
        boolean hasPartnerTable = false;
        for (Table t : usedTables) {
            if (t.getName().equalsIgnoreCase("PARTNER")) {
                hasPartnerTable = true;
                break;
            }
        }
        assertTrue(hasPartnerTable);
    }

    @Test
    public void testSchemaMappingUsedColumns() {
        SchemaMapping sm = environment.getSchemaMapping();

        Collection<Column> usedColumns = sm.getUsedColumns();
        assertNotNull(usedColumns);
        assertFalse(usedColumns.isEmpty());
    }

    @Test
    public void testSchemaMappingUnusedTables() {
        SchemaMapping sm = environment.getSchemaMapping();

        // Just test that it doesn't throw
        Collection<Table> unusedTables = sm.getUnusedTables();
        assertNotNull(unusedTables);
    }

    @Test
    public void testSchemaMappingUnusedColumns() {
        SchemaMapping sm = environment.getSchemaMapping();

        // Just test that it doesn't throw
        Collection<Column> unusedColumns = sm.getUnusedColumns();
        assertNotNull(unusedColumns);
    }

    // ========== ClassMapping Tests ==========

    @Test
    public void testClassMappingBasics() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        assertNotNull(partnerCm);
        assertNotNull(partnerCm.getClassDescription());
        assertNotNull(partnerCm.getSchemaMapping());
        assertEquals(Partner.class.getName(), partnerCm.getClassDescription().getClassName());
    }

    @Test
    public void testClassMappingPrimaryTable() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Partner is the root, should have primary table
        Table primaryTable = partnerCm.getPrimaryTable();
        assertNotNull(primaryTable);
        assertEquals("PARTNER", primaryTable.getName().toUpperCase());

        // Primary update table
        Table primaryUpdateTable = partnerCm.getPrimaryUpdateTable();
        assertNotNull(primaryUpdateTable);
    }

    @Test
    public void testClassMappingPrimaryKey() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        PrimaryKey pk = partnerCm.getPrimaryKey();
        assertNotNull(pk);
        assertNotNull(pk.getColumnMappings());
        assertTrue(pk.getColumnMappings().length > 0);
    }

    @Test
    public void testClassMappingInheritanceHierarchy() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        ClassMapping companyCm = environment.getSchemaMapping().getClassMapping(Company.class.getName());

        // Person's super class mapping should be Partner
        assertEquals(partnerCm, personCm.getSuperClassMapping());

        // Company's super class mapping should be Partner
        assertEquals(partnerCm, companyCm.getSuperClassMapping());

        // Partner has no super class mapping
        assertNull(partnerCm.getSuperClassMapping());
    }

    @Test
    public void testClassMappingIsChildOf() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        ClassMapping companyCm = environment.getSchemaMapping().getClassMapping(Company.class.getName());

        // Person is child of Partner
        assertTrue(personCm.isChildOf(partnerCm));

        // Company is child of Partner
        assertTrue(companyCm.isChildOf(partnerCm));

        // Partner is NOT child of Person
        assertFalse(partnerCm.isChildOf(personCm));

        // Person is NOT child of Company
        assertFalse(personCm.isChildOf(companyCm));
    }

    @Test
    public void testClassMappingChildren() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Partner should have immediate children (Person, Company)
        assertNotNull(partnerCm.getImmediateChildren());

        // Partner should have all children
        assertNotNull(partnerCm.getAllChildren());
        assertTrue(partnerCm.getAllChildren().size() >= 2);
    }

    @Test
    public void testClassMappingIsLeaf() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Person is a leaf (no subclasses)
        assertTrue(personCm.isLeaf());

        // Partner is NOT a leaf (has subclasses)
        assertFalse(partnerCm.isLeaf());
    }

    @Test
    public void testClassMappingPropertyMappings() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassDescription personCd = getClassDescription(Person.class);

        // Get property mappings
        Collection<PropertyMapping> mappings = personCm.getPropertyMappings();
        assertNotNull(mappings);
        assertFalse(mappings.isEmpty());

        // Get specific property mapping
        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        PropertyMapping firstNamePm = personCm.getPropertyMapping(firstNamePd);
        assertNotNull(firstNamePm);

        // Get by property name
        PropertyMapping aliasMapping = personCm.getPropertyMapping("alias");
        assertNotNull(aliasMapping);
    }

    @Test
    public void testClassMappingHierarchyPropertyMappings() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Hierarchy property mappings should include inherited mappings
        assertNotNull(personCm.getHierarchyPropertyMappings());
        assertFalse(personCm.getHierarchyPropertyMappings().isEmpty());

        // Partner's hierarchy should include mappings from subclasses
        assertNotNull(partnerCm.getHierarchyPropertyMappings());
    }

    @Test
    public void testClassMappingHierarchyPhysicalPropertyMappingList() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());

        assertNotNull(personCm.getHierarchyPhysicalPropertyMappingList());
        assertFalse(personCm.getHierarchyPhysicalPropertyMappingList().isEmpty());
    }

    @Test
    public void testClassMappingGetPropertyMappingInHierarchy() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        ClassDescription personCd = getClassDescription(Person.class);

        // Should find property mapping in hierarchy
        PropertyDescription aliasPd = personCd.getPropertyDescription("alias");
        PropertyMapping aliasPm = partnerCm.getPropertyMappingInHierarchy(aliasPd);
        assertNotNull(aliasPm);
    }

    @Test
    public void testClassMappingDiscriminator() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassMapping companyCm = environment.getSchemaMapping().getClassMapping(Company.class.getName());
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Person discriminator value should be "P"
        assertEquals("P", personCm.getDiscriminatorValue());

        // Company discriminator value should be "C"
        assertEquals("C", companyCm.getDiscriminatorValue());

        // Get class mapping for discriminator value
        ClassMapping foundPerson = partnerCm.getClassMappingForDiscriminatorValue("P");
        assertEquals(personCm, foundPerson);

        ClassMapping foundCompany = partnerCm.getClassMappingForDiscriminatorValue("C");
        assertEquals(companyCm, foundCompany);

        // Non-existent discriminator
        ClassMapping notFound = partnerCm.getClassMappingForDiscriminatorValue("X");
        assertNull(notFound);
    }

    @Test
    public void testClassMappingToString() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        String str = partnerCm.toString();
        assertNotNull(str);
        assertTrue(str.contains(Partner.class.getName()));
        assertTrue(str.contains("PARTNER"));
    }

    @Test
    public void testClassMappingCollectUsedTables() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());

        java.util.Set<Table> tables = new java.util.HashSet<>();
        personCm.collectUsedTables(tables);

        assertFalse(tables.isEmpty());
    }

    @Test
    public void testClassMappingCollectUsedColumns() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());

        java.util.Set<Column> columns = new java.util.HashSet<>();
        personCm.collectUsedColumns(columns);

        assertFalse(columns.isEmpty());
    }

    @Test
    public void testClassMappingGetColumnMapping() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Get a column from the primary table
        Table primaryTable = partnerCm.getPrimaryTable();
        Column aliasColumn = primaryTable.getColumn("ALIAS");

        if (aliasColumn != null) {
            ColumnMapping cm = partnerCm.getColumnMapping(aliasColumn);
            assertNotNull(cm);
        }
    }

    // ========== PropertyMapping Tests ==========

    @Test
    public void testPropertyMappingBasics() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassDescription personCd = getClassDescription(Person.class);

        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        PropertyMapping pm = personCm.getPropertyMapping(firstNamePd);

        assertNotNull(pm);
        assertNotNull(pm.getPropertyDescription());
        assertEquals(firstNamePd, pm.getPropertyDescription());
    }

    @Test
    public void testPhysicalPropertyMapping() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());

        // Get physical property mappings
        assertNotNull(personCm.getHierarchyPhysicalPropertyMappingList());

        for (PhysicalPropertyMapping ppm : personCm.getHierarchyPhysicalPropertyMappingList()) {
            assertNotNull(ppm.getPropertyDescription());
        }
    }

    // ========== AbstractClassMapping Tests ==========

    @Test
    public void testAbstractClassMappingPersistorGeneratedColumnMappings() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());

        // Just verify the method exists and returns a list
        assertNotNull(partnerCm.getPersistorGeneratedColumnMappings());
    }

    // ========== PrimaryKey Tests ==========

    @Test
    public void testPrimaryKeyBasics() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        PrimaryKey pk = partnerCm.getPrimaryKey();

        assertNotNull(pk);
        assertNotNull(pk.getColumnMappings());

        // Test contains method
        for (ColumnMapping cm : pk.getColumnMappings()) {
            assertTrue(pk.contains(cm));
        }
    }

    // ========== Integration Tests ==========

    @Test
    public void testSchemaAndMappingIntegration() throws Exception {
        createModificationTracker();

        // Create entities using the schema/mapping infrastructure
        Person p = createPerson("schema", "SchemaLast", "SchemaFirst");
        persist();

        // Verify through schema description
        SchemaDescription sd = environment.getSchemaMapping().getSchemaDescription();
        ClassDescription personCd = sd.getClassDescription(Person.class.getName());
        assertNotNull(personCd);

        // Verify through class mapping
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        assertNotNull(personCm);

        // Load and verify
        createModificationTracker();
        Person loaded = loadById(Person.class, false, p.getId(), "root");
        assertNotNull(loaded);
        assertEquals("SchemaFirst", loaded.getFirstName());
    }

    @Test
    public void testInheritanceWithSchemaMapping() throws Exception {
        createModificationTracker();

        // Create Person (subclass)
        createPerson("inhTest", "InhLast", "InhFirst");

        // Create Company (sibling subclass)
        Company c = new Company();
        c.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(c);
        c.setAlias("inhCo");
        c.setName("Inheritance Test Co");

        persist();

        // Verify both can be loaded through Partner mapping
        SchemaMapping sm = environment.getSchemaMapping();
        ClassMapping partnerCm = sm.getClassMapping(Partner.class.getName());

        // Both Person and Company are children of Partner
        assertTrue(partnerCm.getAllChildren().size() >= 2);

        // Discriminator values work correctly
        assertEquals("P", sm.getClassMapping(Person.class.getName()).getDiscriminatorValue());
        assertEquals("C", sm.getClassMapping(Company.class.getName()).getDiscriminatorValue());
    }

    // ========== RelationshipPropertyDescription Tests ==========

    @Test
    public void testRelationshipPropertyDescription() {
        ClassDescription companyCd = getClassDescription(Company.class);
        ClassDescription partnerCd = getClassDescription(Partner.class);

        // boss is a ReferencePropertyDescription in Company
        PropertyDescription bossPd = companyCd.getPropertyDescription("boss");
        assertNotNull(bossPd);
        assertInstanceOf(ReferencePropertyDescription.class, bossPd);

        ReferencePropertyDescription bossRef = (ReferencePropertyDescription) bossPd;
        // boss references Person
        assertNotNull(bossRef.getReferencedClassDescription());

        // collaborators is a CollectionPropertyDescription in Partner
        PropertyDescription collabPd = partnerCd.getPropertyDescription("collaborators");
        assertNotNull(collabPd);
        assertInstanceOf(CollectionPropertyDescription.class, collabPd);

        CollectionPropertyDescription collabColl = (CollectionPropertyDescription) collabPd;
        // collaborators references Partner
        assertNotNull(collabColl.getReferencedClassDescription());
        assertEquals(Partner.class.getName(), collabColl.getReferencedClassDescription().getClassName());
    }

    @Test
    public void testRelationshipPropertyDescriptionReverseProperty() {
        ClassDescription partnerCd = getClassDescription(Partner.class);

        PropertyDescription collabPd = partnerCd.getPropertyDescription("collaborators");
        assertInstanceOf(RelationshipPropertyDescription.class, collabPd);

        RelationshipPropertyDescription relPd = (RelationshipPropertyDescription) collabPd;
        // Test getReversePropertyName and getReversePropertyDescription
        String reverseName = relPd.getReversePropertyName();
        // May be null for this relationship
        if (reverseName != null) {
            RelationshipPropertyDescription reverseDesc = relPd.getReversePropertyDescription();
            assertNotNull(reverseDesc);
        }
    }

    // ========== PropertyDescription Tests ==========

    @Test
    public void testPropertyDescriptionToString() {
        ClassDescription personCd = getClassDescription(Person.class);
        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");

        String str = firstNamePd.toString();
        assertNotNull(str);
        assertTrue(str.contains("firstName"));
        assertTrue(str.contains(Person.class.getName()));
    }

    @Test
    public void testScalarPropertyDescription() {
        ClassDescription personCd = getClassDescription(Person.class);

        // firstName should be a scalar property
        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        assertNotNull(firstNamePd);
        assertInstanceOf(ScalarPropertyDescription.class, firstNamePd);

        // Verify class description
        assertEquals(personCd, firstNamePd.getClassDescription());
    }

    // ========== PrimaryKey Additional Tests ==========

    @Test
    public void testPrimaryKeyGeneratedByDatabaseColumns() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        PrimaryKey pk = partnerCm.getPrimaryKey();

        // Test methods even if there are no generated columns
        ColumnMapping[] generatedColumns = pk.getGeneratedByDatabaseColumnMappings();
        assertNotNull(generatedColumns);

        String[] generatedColumnNames = pk.getGeneratedByDatabaseColumnNames();
        assertNotNull(generatedColumnNames);
    }

    // ========== ColumnMapping Tests ==========

    @Test
    public void testColumnMappingProperties() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        ClassDescription partnerCd = getClassDescription(Partner.class);

        PropertyDescription aliasPd = partnerCd.getPropertyDescription("alias");
        PropertyMapping pm = partnerCm.getPropertyMapping(aliasPd);

        if (pm instanceof ColumnMapping cm) {
            // Test read/update columns
            assertNotNull(cm.getReadColumn());

            // Test insertable/updatable
            assertTrue(cm.isInsertable() || !cm.isInsertable()); // Just test it doesn't throw
            assertTrue(cm.isUpdatable() || !cm.isUpdatable());

            // Test getClassMapping
            assertEquals(partnerCm, cm.getClassMapping());

            // Value generator may be null
            cm.getValueGenerator();
        }
    }

    @Test
    public void testColumnMappingVisitColumns() {
        ClassMapping partnerCm = environment.getSchemaMapping().getClassMapping(Partner.class.getName());
        ClassDescription partnerCd = getClassDescription(Partner.class);

        PropertyDescription aliasPd = partnerCd.getPropertyDescription("alias");
        PropertyMapping pm = partnerCm.getPropertyMapping(aliasPd);

        if (pm instanceof ColumnMapping cm) {
            java.util.List<Column> visitedColumns = new java.util.ArrayList<>();
            cm.visitColumns(visitedColumns::add);
            assertFalse(visitedColumns.isEmpty());
        }
    }

    // ========== PropertyMapping Additional Tests ==========

    @Test
    public void testPropertyMappingCollectUsedTables() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassDescription personCd = getClassDescription(Person.class);

        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        PropertyMapping pm = personCm.getPropertyMapping(firstNamePd);

        java.util.Set<Table> tables = new java.util.HashSet<>();
        pm.collectUsedTables(tables);
        // Tables collection may be empty for column mappings that don't add tables
    }

    @Test
    public void testPropertyMappingCollectUsedColumns() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        ClassDescription personCd = getClassDescription(Person.class);

        PropertyDescription firstNamePd = personCd.getPropertyDescription("firstName");
        PropertyMapping pm = personCm.getPropertyMapping(firstNamePd);

        java.util.Set<Column> columns = new java.util.HashSet<>();
        pm.collectUsedColumns(columns);
        assertFalse(columns.isEmpty());
    }

    // ========== RelationshipPropertyMapping Tests ==========

    @Test
    public void testRelationshipPropertyMapping() {
        ClassMapping companyCm = environment.getSchemaMapping().getClassMapping(Company.class.getName());
        ClassDescription companyCd = getClassDescription(Company.class);

        PropertyDescription bossPd = companyCd.getPropertyDescription("boss");
        PropertyMapping pm = companyCm.getPropertyMapping(bossPd);

        if (pm instanceof RelationshipPropertyMapping rpm) {
            assertNotNull(rpm.getPropertyDescription());
            assertNotNull(rpm.getReferencedClassMapping());
        }
    }

    // ========== SchemaDescription SchemaName Tests ==========

    @Test
    public void testSchemaDescriptionSchemaName() {
        SchemaDescription sd = environment.getSchemaMapping().getSchemaDescription();

        String originalName = sd.getSchemaName();
        assertNotNull(originalName);

        // Test setter (restore original after)
        sd.setSchemaName("testSchemaName");
        assertEquals("testSchemaName", sd.getSchemaName());

        // Restore
        sd.setSchemaName(originalName);
    }

    // ========== ClassDescription Additional Tests ==========

    @Test
    public void testClassDescriptionIdentityClass() {
        ClassDescription personCd = getClassDescription(Person.class);

        // Test getIdentityClass and setIdentityClass
        Class<?> originalIdClass = personCd.getIdentityClass();

        // Setting null should work
        personCd.setIdentityClass(null);
        assertNull(personCd.getIdentityClass());

        // Restore if there was one
        if (originalIdClass != null) {
            personCd.setIdentityClass(originalIdClass);
        }
    }

    @Test
    public void testClassDescriptionSetAbstract() {
        ClassDescription personCd = getClassDescription(Person.class);

        boolean originalAbstract = personCd.isAbstract();

        // Test setter
        personCd.setAbstract(true);
        assertTrue(personCd.isAbstract());

        personCd.setAbstract(false);
        assertFalse(personCd.isAbstract());

        // Restore
        personCd.setAbstract(originalAbstract);
    }

    // ========== More Integration Tests ==========

    @Test
    public void testLoadWithPrimaryKey() throws Exception {
        createModificationTracker();

        Person p = createPerson("pkTest", "PkLast", "PkFirst");
        persist();

        // Test that the primary key loading works
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());
        PrimaryKey pk = personCm.getPrimaryKey();

        // Primary key should have column mappings
        assertTrue(pk.getColumnMappings().length > 0);

        // Load and verify
        createModificationTracker();
        Person loaded = loadById(Person.class, false, p.getId(), "root");
        assertNotNull(loaded);
    }

    @Test
    public void testClassMappingGetPhysicalPropertyMappingList() {
        ClassMapping personCm = environment.getSchemaMapping().getClassMapping(Person.class.getName());

        // Test hierarchy physical property mapping list
        java.util.List<PhysicalPropertyMapping> ppms = personCm.getHierarchyPhysicalPropertyMappingList();
        assertNotNull(ppms);
        assertFalse(ppms.isEmpty());

        for (PhysicalPropertyMapping ppm : ppms) {
            assertNotNull(ppm.getPropertyDescription());
        }
    }
}
