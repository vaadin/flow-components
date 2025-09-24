/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.upload.receivers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated(since = "24.8", forRemoval = true)
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
