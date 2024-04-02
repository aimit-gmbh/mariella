package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.UnitInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// TODO validate with schema validator, aquire persistence-schema-xml
public class PersistenceXmlHandler extends DefaultHandler {

    private final List<UnitInfo> unitInfos = new ArrayList<>();
    private UnitInfo unitInfo;
    private String curValue;

    public List<UnitInfo> getUnitInfos() {
        return unitInfos;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (localName.equals("persistence-unit")) {
                unitInfo = new UnitInfo();
                unitInfos.add(unitInfo);
                unitInfo.setPersistenceUnitName(attributes.getValue("name"));
            }
            if (localName.equals("property")) {
                String name = attributes.getValue("name");
                String value = attributes.getValue("value");
                unitInfo.getProperties().put(name, value);
            }
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (localName.equals("jar-file")) {
                URL url = new URI(curValue).toURL();
                unitInfo.getJarFileUrls().add(url);
            } else if (localName.equals("class")) {
                unitInfo.getManagedClassNames().add(curValue);
            }

        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        curValue = new String(ch, start, length);
    }

    public UnitInfo getUnitInfo() {
        return unitInfo;
    }

}
