package org.mariella.persistence.annotations_processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DirectoryClasspathBrowser extends ClasspathBrowser {

    public DirectoryClasspathBrowser(File dir) {
        read(dir);
    }

    private void read(File dir) {
        try {
            File[] dirfiles = dir.listFiles();
            if (dirfiles == null)
                return;
            for (File dirfile : dirfiles) {
                if (dirfile.isDirectory()) {
                    read(dirfile);
                } else {
                    if (dirfile.getName().endsWith(".class")) {
                        Entry entry = new Entry();
                        entry.setName(dirfile.getName());
                        entry.setInputStream(new FileInputStream(dirfile));
                        entries.add(entry);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Directory does not exist: " + dir);
        }
    }

}
