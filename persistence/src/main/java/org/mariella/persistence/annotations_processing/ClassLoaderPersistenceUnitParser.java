package org.mariella.persistence.annotations_processing;

import com.google.common.reflect.ClassPath;
import org.mariella.persistence.annotations_processing.ClasspathBrowser.Entry;
import org.mariella.persistence.mapping.UnitInfo;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class ClassLoaderPersistenceUnitParser implements PersistenceUnitParser {
    private final List<UnitInfo> unitInfos = new ArrayList<>();
    private final ClassLoader classLoader;


    public ClassLoaderPersistenceUnitParser(ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
    }

    @Override
    public Class<?> loadClass(Entry entry, String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    protected List<String> getPersistentClassNames(UnitInfo unitInfo) {
        String s = (String) unitInfo.getProperties().get("org.mariella.persistence.packages");
        if (s == null) {
            throw new RuntimeException("Property 'org.mariella.persistence.packages' is missing in persistence.xml");
        }
        List<String> packageNames = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(s, ",");
        while (tokenizer.hasMoreTokens()) {
            String packageName = tokenizer.nextToken().trim().toLowerCase();
            packageNames.add(packageName);
        }
        try {
            return ClassPath.from(classLoader)
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> packageNames.contains(clazz.getPackageName()))
                    .map(ClassPath.ClassInfo::getName)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Entry> readEntries(UnitInfo unitInfo) {

        List<Entry> entries = new ArrayList<>();

        getPersistentClassNames(unitInfo).forEach(cn -> {
            Entry entry = new Entry();
            InputStream is = classLoader.getResourceAsStream(cn);
            entry.setInputStream(is);
            entry.setName(cn);
            entries.add(entry);
        });
        return entries;
    }


    @Override
    public void parsePersistenceUnits() throws Exception {
    	InputStream is = createPersistencUnitInputStream();
    	if(is != null) {
    		parsePersistenceXml(null, is);
    	} else {
	    	Enumeration<URL> e = classLoader.getResources("META-INF/persistence.xml");
	    	while(e.hasMoreElements()) {
		        URL url = e.nextElement();
		        is = createPersistencUnitInputStream(url);
		        parsePersistenceXml(url, is);
	    	}
    	}
    }

    private void parsePersistenceXml(URL url, InputStream is) throws Exception {
        PersistenceXmlHandler handler = new PersistenceXmlHandler();
        URL rootUrl = getRootUrl(url);

        @SuppressWarnings("deprecation") XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(is));

        List<UnitInfo> infos = handler.getUnitInfos();
        for (UnitInfo unitInfo : infos) {
            unitInfo.setPersistenceUnitRootUrl(rootUrl);
        }

        this.unitInfos.addAll(infos);
    }
    

    protected InputStream createPersistencUnitInputStream() throws Exception {
    	return null;
    }
    
    protected InputStream createPersistencUnitInputStream(URL url) throws Exception {
        InputStream persistenceXmlIs = url.openStream();
        if (persistenceXmlIs == null) {
            throw new Exception("Could not find " + url);
        }
        return persistenceXmlIs;
    }

    protected URL getRootUrl(URL url) throws Exception {
        File file = new File(urlDecode(url.getFile()));
        File rootFile = file.getParentFile().getParentFile();
        URL rootUrl;
        if (rootFile.getPath().endsWith("BOOT-INF")) {
            rootUrl = url;
        } else {
            rootUrl = new URI("file:", null, rootFile.getPath()).toURL();
        }
        return rootUrl;
    }


    @Override
    public List<UnitInfo> getUnitInfos() {
        return unitInfos;
    }

    private String urlDecode(String file) {
        return URLDecoder.decode(file, StandardCharsets.UTF_8);
    }

}
