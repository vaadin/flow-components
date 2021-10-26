package com.vaadin.flow.component.upload.receivers;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempDirectory implements Serializable {
    private transient Path tempPath;

    private TempDirectory() {
        try {
            tempPath = Files.createTempDirectory("temp_dir");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE,
                    "Failed to create temporary directory for upload component '",
                    e);
            tempPath = null;
        }
    }

    private static class LazyHolder implements Serializable {
        static final TempDirectory INSTANCE = new TempDirectory();
    }

    public Object readResolve() {
        return getInstance();
    }

    public static TempDirectory getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Path getTempPath() {
        return tempPath;
    }

    private Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }
}
