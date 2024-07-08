/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class AbstractUploadIT extends AbstractComponentIT {
    /**
     * @return The generated temp file handle
     * @throws IOException
     */
    File createTempFile() throws IOException {
        File tempFile = File.createTempFile("TestFileUpload", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(getTempFileContents());
        writer.close();
        tempFile.deleteOnExit();
        return tempFile;
    }

    String getTempFileContents() {
        return "This is a test file! Row 2 Row3";
    }

    void fillPathToUploadInput(WebElement input, String... tempFileNames)
            throws Exception {
        // create a valid path in upload input element. Instead of selecting a
        // file by some file browsing dialog, we use the local path directly.
        setLocalFileDetector(input);
        input.sendKeys(String.join(System.lineSeparator(), tempFileNames));
    }

    private void setLocalFileDetector(WebElement element) throws Exception {
        if (getRunLocallyBrowser() != null) {
            return;
        }

        if (element instanceof WrapsElement) {
            element = ((WrapsElement) element).getWrappedElement();
        }
        if (element instanceof RemoteWebElement) {
            ((RemoteWebElement) element)
                    .setFileDetector(new LocalFileDetector());
        } else {
            throw new IllegalArgumentException(
                    "Expected argument of type RemoteWebElement, received "
                            + element.getClass().getName());
        }
    }

    WebElement getInput(TestBenchElement upload) {
        return upload.$("*").id("fileInput");
    }
}