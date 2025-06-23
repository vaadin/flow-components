/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

/**
 * Upload component test class.
 */
@TestPath("vaadin-upload")
public class UploadIT extends AbstractUploadIT {

    @Test
    public void testUploadAnyFile() throws Exception {
        open();

        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();
        getUpload().upload(tempFile);

        WebElement uploadOutput = getDriver().findElement(By.id("test-output"));

        String content = uploadOutput.getText();

        String expectedContent = tempFile.getName() + getTempFileContents();

        Assert.assertEquals("Upload content does not match expected",
                expectedContent, content);
    }

    @Test
    public void testClearFileList() throws Exception {
        open();

        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();

        getUpload().upload(tempFile);
        getUpload().upload(tempFile);
        getUpload().upload(tempFile);

        $("button").id("print-file-list").click();

        Assert.assertNotEquals("File list should contain files", "[]",
                $("div").id("file-list").getText());

        $("button").id("clear-file-list").click();
        $("button").id("print-file-list").click();

        Assert.assertEquals("File list should not contain files", "[]",
                $("div").id("file-list").getText());
    }

    @Test
    public void testUploadMultipleEventOrder() throws Exception {
        open();

        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();

        getUpload().uploadMultiple(List.of(tempFile, tempFile, tempFile), 10);

        WebElement eventsOutput = getDriver()
                .findElement(By.id("test-events-output"));

        Assert.assertEquals("Upload event order does not match expected",
                "-succeeded-succeeded-succeeded-finished",
                eventsOutput.getText());
    }

    @Test
    public void testUploadEventOrder() throws Exception {
        open();

        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();

        getUpload().upload(tempFile);

        WebElement eventsOutput = getDriver()
                .findElement(By.id("test-events-output"));

        Assert.assertEquals("Upload event order does not match expected",
                "-succeeded-finished", eventsOutput.getText());
    }

    @Test
    public void uploadFileAndNoErrorThrown() throws Exception {
        open();
        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();
        getUpload().upload(tempFile);

        List<LogEntry> logList1 = getLogEntries(Level.SEVERE);
        assertThat("There should have no severe message in the console",
                logList1.size(), CoreMatchers.is(0));

        WebElement upload = getUpload();
        executeScript("arguments[0]._removeFile()", upload);
        List<LogEntry> logList2 = getLogEntries(Level.SEVERE);
        assertThat("There should have no severe message in the console",
                logList2.size(), CoreMatchers.is(0));
    }

    private UploadElement getUpload() {
        return $(UploadElement.class).id("test-upload");
    }

}
