/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * Tests for Upload using FileBuffer and MultiFileBuffer.
 */
@TestPath("vaadin-upload/filebuffer")
public class FileBufferIT extends AbstractUploadIT {
    @Test
    public void testUploadAnyFile() throws Exception {
        open();
        final UploadElement upload = $(UploadElement.class).id("single-upload");
        waitUntil(driver -> upload.isDisplayed());

        File tempFile = createTempFile();
        fillPathToUploadInput(getInput(upload), tempFile.getPath());

        WebElement uploadOutput = getDriver()
                .findElement(By.id("single-upload-output"));

        String content = uploadOutput.getText();

        String expectedContent = tempFile.getName() + getTempFileContents();

        Assert.assertEquals("Upload content does not match expected",
                expectedContent, content);
    }

    @Test
    public void testUploadMultipleEventOrder() throws Exception {
        if (getRunLocallyBrowser() == null) {
            // Multiple file upload does not work with Remotewebdriver
            // https://github.com/SeleniumHQ/selenium/issues/7408
            throw new AssumptionViolatedException(
                    "Skipped <Multiple file upload does not work with Remotewebdriver>");
        }
        open();

        final UploadElement upload = $(UploadElement.class).id("multi-upload");
        waitUntil(driver -> upload.isDisplayed());

        File tempFile = createTempFile();

        fillPathToUploadInput(getInput(upload), tempFile.getPath(),
                tempFile.getPath(), tempFile.getPath());

        WebElement eventsOutput = getDriver()
                .findElement(By.id("multi-upload-event-output"));

        Assert.assertEquals("Upload event order does not match expected",
                "-succeeded-succeeded-succeeded-finished",
                eventsOutput.getText());
    }
}
