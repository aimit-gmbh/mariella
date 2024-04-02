package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.annotations_processing.ClasspathBrowser.Entry;
import org.mariella.persistence.mapping.UnitInfo;

import java.util.List;

public interface PersistenceUnitParser {
    String PERSISTENCE_XML_LOCATION = "META-INF/persistence.xml";

    List<UnitInfo> getUnitInfos();

    List<Entry> readEntries(UnitInfo unitInfo);

    void parsePersistenceUnits() throws Exception;

    Class<?> loadClass(Entry entry, String className) throws ClassNotFoundException;

}