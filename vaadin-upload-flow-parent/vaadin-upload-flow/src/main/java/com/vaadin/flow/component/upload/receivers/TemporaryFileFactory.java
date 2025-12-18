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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Deprecated(since = "24.8", forRemoval = true)
public class TemporaryFileFactory implements FileFactory {

    /**
     * Create a new temporary file
     */
    @Override
    public File createFile(String fileName) throws IOException {

        final String tempFileName = "upload_temp_file";
        Path tempDirectory = TempDirectory.getPath();

        if (tempDirectory == null) {
            throw new IOException("Failed to create temp directory");
        }
        return Files.createTempFile(tempDirectory, tempFileName, "tmp")
                .toFile();
    }
}
