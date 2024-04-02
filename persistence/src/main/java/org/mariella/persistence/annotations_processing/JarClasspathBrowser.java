package org.mariella.persistence.annotations_processing;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarClasspathBrowser extends ClasspathBrowser {
    final File file;
    ZipFile zip;

    public JarClasspathBrowser(File f) {
        this.file = f;
        read();
    }

    private void read() {
        try {
            zip = new ZipFile(file);
            for (@SuppressWarnings("rawtypes")
                 Enumeration zipentries = zip.entries(); zipentries.hasMoreElements(); ) {
                ZipEntry zentry = (ZipEntry) zipentries.nextElement();
                if (zentry.isDirectory()) continue;
                if (!zentry.getName().endsWith(".class")) continue;


                Entry entry = new Entry();
                entry.setInputStream(zip.getInputStream(zentry));
                entry.setName(zentry.getName());
                entries.add(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
