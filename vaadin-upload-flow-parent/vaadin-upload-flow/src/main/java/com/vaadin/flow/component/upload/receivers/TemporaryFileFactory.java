package com.vaadin.flow.component.upload.receivers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemporaryFileFactory implements FileFactory {

    static Path tempDirectory = null;

    /**
     * Create a new temporary file
     */
    @Override
    public File createFile(String fileName) throws IOException {
        if (tempDirectory == null) {
            tempDirectory = Files.createTempDirectory("temp_directory");
        }
        final String tempFileName = "upload_temp_file";
        return Files.createTempFile(tempDirectory, tempFileName, null).toFile();
    }
}
