package org.mariella.test;

import org.junit.jupiter.api.Test;
import org.mariella.persistence.runtime.ModificationInfo;
import org.mariella.persistence.runtime.TrackedList;
import org.mariella.test.model.Company;
import org.mariella.test.model.Person;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeTest extends AbstractSimpleTest {

    @Test
    public void testTrackedListAdd() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        assertTrue(list.isEmpty());

        list.add("first");
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        assertEquals("first", list.getFirst());

        // Adding duplicate should return false (ObservableList behavior)
        boolean added = list.add("first");
        assertFalse(added);
        assertEquals(1, list.size());

        list.add("second");
        assertEquals(2, list.size());
    }

    @Test
    public void testTrackedListAddAtIndex() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("third");
        list.add(1, "second");

        assertEquals(3, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
        assertEquals("third", list.get(2));
    }

    @Test
    public void testTrackedListRemove() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");
        list.add("third");

        assertTrue(list.remove("second"));
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("third", list.get(1));

        // Remove by index
        String removed = list.removeFirst();
        assertEquals("first", removed);
        assertEquals(1, list.size());

        // Remove non-existent
        assertFalse(list.remove("nonexistent"));
    }

    @Test
    public void testTrackedListRemoveAll() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");
        list.add("third");
        list.add("fourth");

        boolean modified = list.removeAll(Arrays.asList("second", "fourth"));
        assertTrue(modified);
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("third", list.get(1));

        // Remove non-existent items
        modified = list.removeAll(Arrays.asList("nonexistent1", "nonexistent2"));
        assertFalse(modified);
    }

    @Test
    public void testTrackedListAddAll() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        boolean modified = list.addAll(Arrays.asList("first", "second", "third"));
        assertTrue(modified);
        assertEquals(3, list.size());
    }

    @Test
    public void testTrackedListSet() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");

        String old = list.set(1, "replaced");
        assertEquals("second", old);
        assertEquals("replaced", list.get(1));
    }

    @Test
    public void testTrackedListContains() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");

        assertTrue(list.contains("first"));
        assertTrue(list.contains("second"));
        assertFalse(list.contains("third"));
    }

    @Test
    public void testTrackedListIndexOf() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");

        assertEquals(0, list.indexOf("first"));
        assertEquals(1, list.indexOf("second"));
        assertEquals(-1, list.indexOf("nonexistent"));

        assertEquals(0, list.lastIndexOf("first"));
        assertEquals(1, list.lastIndexOf("second"));
    }

    @Test
    public void testTrackedListIterator() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");
        list.add("third");

        List<String> collected = new ArrayList<>();
        for (String s : list) {
            collected.add(s);
        }

        assertEquals(3, collected.size());
        assertEquals("first", collected.get(0));
        assertEquals("second", collected.get(1));
        assertEquals("third", collected.get(2));
    }

    @Test
    public void testTrackedListSubList() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");
        list.add("third");
        list.add("fourth");

        List<String> subList = list.subList(1, 3);
        assertEquals(2, subList.size());
        assertEquals("second", subList.get(0));
        assertEquals("third", subList.get(1));
    }

    @Test
    public void testTrackedListToArray() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");

        Object[] arr = list.toArray();
        assertEquals(2, arr.length);
        assertEquals("first", arr[0]);
        assertEquals("second", arr[1]);

        // Note: toArray(E[]) returns Object[] due to implementation
        Object[] objArr = list.toArray(new Object[0]);
        assertEquals(2, objArr.length);
    }

    @Test
    public void testTrackedListToString() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");

        String str = list.toString();
        assertNotNull(str);
        assertTrue(str.contains("first"));
        assertTrue(str.contains("second"));
    }

    @Test
    public void testTrackedListPropertyName() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "myPropertyName");
        assertEquals("myPropertyName", list.getPropertyName());
    }

    @Test
    public void testTrackedListModCount() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        int initialModCount = list.getModCount();
        list.add("first");
        assertTrue(list.getModCount() > initialModCount);

        int modCountAfterAdd = list.getModCount();
        list.remove("first");
        assertTrue(list.getModCount() > modCountAfterAdd);
    }

    @Test
    public void testTrackedListMoveAll() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");

        // Move elements at index 1-2 (b, c) to index 4
        list.moveAll(1, 2, 4);

        assertEquals("a", list.get(0));
        assertEquals("d", list.get(1));
        assertEquals("e", list.get(2));
        assertEquals("b", list.get(3));
        assertEquals("c", list.get(4));
    }

    @Test
    public void testTrackedListPrimitiveClear() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");
        list.add("second");
        assertEquals(2, list.size());

        list.primitiveClear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    public void testTrackedListUnsupportedOperations() {
        TrackedList<String> list = new TrackedList<>(new java.beans.PropertyChangeSupport(this), "testList");

        list.add("first");

        assertThrows(UnsupportedOperationException.class, list::clear);
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, Arrays.asList("a", "b")));
        assertThrows(UnsupportedOperationException.class, () -> list.containsAll(Arrays.asList("a", "b")));
        assertThrows(UnsupportedOperationException.class, () -> list.retainAll(Arrays.asList("a", "b")));
    }

    @Test
    public void testPropertyChangeListener() {
        Person p = new Person();
        p.setId(UUID.randomUUID());

        AtomicInteger changeCount = new AtomicInteger(0);
        PropertyChangeListener listener = evt -> changeCount.incrementAndGet();

        p.addPropertyChangeListener(listener);

        p.setFirstName("Test");
        assertEquals(1, changeCount.get());

        p.setLastName("User");
        assertEquals(2, changeCount.get());

        p.removePropertyChangeListener(listener);
        p.setAlias("tAlias");
        assertEquals(2, changeCount.get()); // No increment after removal
    }

    @Test
    public void testModificationTrackerNewParticipant() throws Exception {
        createModificationTracker();

        Person p = new Person();
        p.setId(UUID.randomUUID());

        assertNull(modificationTracker.getModificationInfo(p));

        modificationTracker.addNewParticipant(p);

        ModificationInfo info = modificationTracker.getModificationInfo(p);
        assertNotNull(info);
        assertEquals(ModificationInfo.Status.New, info.getStatus());
    }

    @Test
    public void testModificationTrackerRemove() throws Exception {
        createModificationTracker();

        Person p = new Person();
        p.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(p);
        p.setAlias("tRemove");
        p.setFirstName("RemoveFirst");
        p.setLastName("RemoveLast");

        persist();

        // After persist, the participant may still exist with a different status
        // Mark for removal
        modificationTracker.remove(p);

        ModificationInfo info = modificationTracker.getModificationInfo(p);
        assertNotNull(info);
        assertEquals(ModificationInfo.Status.Removed, info.getStatus());
    }

    @Test
    public void testModificationTrackerDirty() throws Exception {
        createModificationTracker();

        Person p = new Person();
        p.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(p);
        p.setAlias("tDirty");
        p.setFirstName("DirtyFirst");
        p.setLastName("DirtyLast");

        persist();

        // After persist, should not be dirty
        assertFalse(modificationTracker.isDirty(p));

        // Make a change
        p.setFirstName("ChangedFirst");

        // Now should be dirty
        assertTrue(modificationTracker.isDirty(p));
    }

    @Test
    public void testCollaboratorsRelationship() throws Exception {
        createModificationTracker();

        Person p1 = createPerson("collab1", "CollabLast1", "CollabFirst1");
        Person p2 = createPerson("collab2", "CollabLast2", "CollabFirst2");
        Company c = new Company();
        c.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(c);
        c.setAlias("collabCo");
        c.setName("Collab Company Inc");

        // Add collaborators
        p1.getCollaborators().add(p2);
        p1.getCollaborators().add(c);

        assertEquals(2, p1.getCollaborators().size());

        persist();

        // Reload and verify
        createModificationTracker();
        Person loaded = loadById(Person.class, false, p1.getId(), "root", "root.collaborators");

        assertNotNull(loaded);
        assertEquals(2, loaded.getCollaborators().size());
    }

    @Test
    public void testCompanyBossRelationship() throws Exception {
        createModificationTracker();

        Person boss = createPerson("boss", "BossLast", "BossFirst");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        modificationTracker.addNewParticipant(company);
        company.setAlias("bossCo");
        company.setName("Boss Company LLC");
        company.setBoss(boss);

        persist();

        // Reload and verify
        createModificationTracker();
        Company loaded = loadById(Company.class, false, company.getId(), "root", "root.boss");

        assertNotNull(loaded);
        assertNotNull(loaded.getBoss());
        assertEquals("BossFirst", loaded.getBoss().getFirstName());
    }

    @Test
    public void testModificationTrackerGetParticipants() throws Exception {
        createModificationTracker();

        createPerson("part1", "PartLast1", "PartFirst1");
        createPerson("part2", "PartLast2", "PartFirst2");

        // getParticipants returns the collection of participants
        // The number depends on implementation - just check it's accessible
        assertNotNull(modificationTracker.getParticipants());
    }

    @Test
    public void testModificationTrackerEnabled() throws Exception {
        createModificationTracker();

        assertTrue(modificationTracker.isEnabled());

        modificationTracker.setEnabled(false);
        assertFalse(modificationTracker.isEnabled());

        modificationTracker.setEnabled(true);
        assertTrue(modificationTracker.isEnabled());
    }

    @Test
    public void testModificationTrackerIsDirtyGlobal() throws Exception {
        createModificationTracker();

        Person p = createPerson("dGlobal", "DirtyGlobalLast", "DirtyGlobalFirst");

        // New entity means tracker is dirty
        assertTrue(modificationTracker.isDirty());

        persist();

        // After persist, no longer dirty
        assertFalse(modificationTracker.isDirty());

        // Make a change
        p.setFirstName("Changed");

        // Now dirty again
        assertTrue(modificationTracker.isDirty());
    }

    @Test
    public void testModificationTrackerGetModifications() throws Exception {
        createModificationTracker();

        Person p1 = createPerson("mod1", "ModLast1", "ModFirst1");
        Person p2 = createPerson("mod2", "ModLast2", "ModFirst2");

        List<ModificationInfo> modifications = modificationTracker.getModifications();
        assertEquals(2, modifications.size());
    }
}
