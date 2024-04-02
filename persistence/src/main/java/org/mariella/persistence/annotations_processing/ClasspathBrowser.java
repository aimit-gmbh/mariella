package org.mariella.persistence.annotations_processing;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class ClasspathBrowser {
    final List<Entry> entries = new ArrayList<>();

    public static List<Entry> readEntries(URL url) {
        if (url.getProtocol().equals("jar")) {
            String fileName = url.getFile();
            if (fileName.endsWith("!/")) {
                fileName = fileName.substring(0, fileName.length() - 2);
            }
            if (fileName.startsWith("file:")) {
                fileName = fileName.substring(5);
            }
            return new JarClasspathBrowser(new File(fileName)).entries;
        } else if (url.getProtocol().equals("file")) {
            String fName = toFileName(url);
            File f = new File(fName);
            if (f.isDirectory()) {
                return new DirectoryClasspathBrowser(f).entries;
            } else {
                return new JarClasspathBrowser(f).entries;
            }
        } else {
            throw new IllegalArgumentException("Invalid url: " + url);
        }
    }

    private static String toFileName(URL url) {
        String fName = url.getFile();
        fName = URLDecoder.decode(fName, StandardCharsets.UTF_8);
        return fName;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry {
        private String name;
        private InputStream inputStream;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }
    }

}
