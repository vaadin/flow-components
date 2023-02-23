package com.vaadin.flow.component.upload.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractUploadIT extends AbstractComponentIT {

    /**
     * Creates a temp file with the provided extension for testing purposes.
     *
     * @param extension
     *            the temp file extension without leading dot
     * @return The generated temp file handle
     * @throws IOException
     */
    File createTempFile(String extension) throws IOException {
        File tempFile = File.createTempFile("TestFileUpload", "." + extension);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(getTempFileContents());
        writer.close();
        tempFile.deleteOnExit();
        return tempFile;
    }

    String getTempFileContents() {
        return "This is a test file! Row 2 Row3";
    }

}
