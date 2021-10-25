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
        Path tempPath = Files.createTempDirectory("temp_directory");

        return Files.createTempFile(tempPath, tempFileName, null).toFile();
    }
}
