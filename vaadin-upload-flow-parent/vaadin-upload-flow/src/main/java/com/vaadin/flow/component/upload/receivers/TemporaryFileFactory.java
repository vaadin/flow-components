/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.upload.receivers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
