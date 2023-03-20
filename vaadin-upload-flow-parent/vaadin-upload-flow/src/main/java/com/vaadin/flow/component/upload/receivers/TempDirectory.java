/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload.receivers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TempDirectory {
    private final Path path;

    private TempDirectory() {
        Path tempPath;
        try {
            tempPath = Files.createTempDirectory("temp_dir");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE,
                    "Failed to create temporary directory for upload component '",
                    e);
            tempPath = null;
        }
        path = tempPath;
    }

    /**
     * This class is for having a thread-safe singleton object. please take a
     * look here:
     * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    private static class LazyHolder {
        static final TempDirectory INSTANCE = new TempDirectory();
    }

    public static Path getPath() {
        return LazyHolder.INSTANCE.path;
    }

    private Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }
}
