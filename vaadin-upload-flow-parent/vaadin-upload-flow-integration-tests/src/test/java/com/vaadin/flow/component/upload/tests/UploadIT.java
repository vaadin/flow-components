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
package com.vaadin.flow.component.upload.tests;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

/**
 * Upload component test class.
 */
@TestPath("vaadin-upload")
public class UploadIT extends AbstractUploadIT {

    private WebElement uploadOutput;

    private WebElement eventsOutput;

    @Before
    public void init() {
        open();
        waitUntil(driver -> getUpload().isDisplayed());
        uploadOutput = getDriver().findElement(By.id("test-output"));
        eventsOutput = getDriver().findElement(By.id("test-events-output"));
    }

    @Test
    public void testUploadAnyFile() throws Exception {
        File tempFile = createTempFile("txt");
        getUpload().upload(tempFile);

        String content = uploadOutput.getText();

        Assert.assertTrue("Upload content does not contain file details",
                content.contains(tempFile.getName() + getTempFileContents()));
        Assert.assertTrue("Progress update event was not fired properly",
                content.contains("PROGRESS:" + tempFile.getName()));
    }

    @Test
    public void testClearFileList() throws Exception {
        File tempFile = createTempFile("txt");

        getUpload().upload(tempFile);
        getUpload().upload(tempFile);
        getUpload().upload(tempFile);

        $("button").id("print-file-count").click();

        Assert.assertEquals("File list should contain 3 files", 3,
                getFileCount());

        $("button").id("clear-file-list").click();
        $("button").id("print-file-count").click();

        Assert.assertEquals("File list should not contain files", 0,
                getFileCount());
    }

    @Test
    public void testUploadMultipleEventOrder() throws Exception {
        File tempFile = createTempFile("txt");

        getUpload().uploadMultiple(List.of(tempFile, tempFile, tempFile), 10);

        Assert.assertEquals("Upload event order does not match expected",
                "-succeeded-succeeded-succeeded-finished",
                eventsOutput.getText());
    }

    @Test
    public void testUploadEventOrder() throws Exception {
        File tempFile = createTempFile("txt");

        getUpload().upload(tempFile);

        Assert.assertEquals("Upload event order does not match expected",
                "-succeeded-finished", eventsOutput.getText());
    }

    @Test
    public void uploadInvalidFile_fileIsRejected() throws Exception {
        File invalidFile = createTempFile("pdf");

        getUpload().upload(invalidFile);

        WebElement eventsOutput = getDriver()
                .findElement(By.id("test-events-output"));
        Assert.assertEquals("Invalid file was not rejected", "-rejected",
                eventsOutput.getText());

        WebElement uploadOutput = getDriver().findElement(By.id("test-output"));
        Assert.assertTrue("Rejected file name was incorrect", uploadOutput
                .getText().contains("REJECTED:" + invalidFile.getName()));
    }

    @Test
    public void uploadFile_removeFile_fileIsRemoved() throws Exception {
        File tempFile = createTempFile("txt");

        getUpload().upload(tempFile);

        $("button").id("print-file-count").click();

        Assert.assertEquals("File list should contain one file", 1,
                getFileCount());

        getUpload().removeFile(0);

        $("button").id("print-file-count").click();

        Assert.assertEquals("File list should not contain files", 0,
                getFileCount());

        Assert.assertEquals("File was not properly removed",
                "-succeeded-finished-removed", eventsOutput.getText());

        Assert.assertTrue("Removed file name was incorrect", uploadOutput
                .getText().contains("REMOVED:" + tempFile.getName()));
    }

    @Test
    public void uploadFileAndNoErrorThrown() throws Exception {
        File tempFile = createTempFile("txt");
        getUpload().upload(tempFile);

        List<LogEntry> logList1 = getLogEntries(Level.SEVERE);
        Assert.assertEquals(
                "There should have no severe message in the console", 0,
                logList1.size());

        WebElement upload = getUpload();
        executeScript("arguments[0]._removeFile()", upload);
        List<LogEntry> logList2 = getLogEntries(Level.SEVERE);
        Assert.assertEquals(
                "There should have no severe message in the console", 0,
                logList2.size());
    }

    @Test
    public void slowUpload_waitForUpload_pollsUntilUploadFinishes()
            throws Exception {
        Assume.assumeTrue("Current driver does not support Dev Tools",
                driver instanceof HasDevTools);

        // Fake slow upload to test whether UploadElement.waitForUploads
        // actually waits for the specified time. UploadElement.waitForUploads
        // checks whether all files are uploaded. To simulate slow upload, first
        // add a file without waiting and then mark it as uploading. Then
        // schedule a script to clear the uploading state after some time to
        // make waitForUploads resolve. Finally upload another file that waits
        // for upload to finish.
        getUpload().setProperty("noAuto", true);
        getUpload().upload(createTempFile("txt"));
        executeScript("arguments[0].files[0].uploading = true", getUpload());
        executeScript(
                "setTimeout(() => arguments[0].files.forEach(file => { file.uploading = false }), 8000)",
                getUpload());

        getUpload().upload(createTempFile("txt"), 10);

        $("button").id("print-file-count").click();

        Assert.assertEquals("File list should contain two files", 2,
                getFileCount());
    }

    private int getFileCount() {
        return Integer.parseInt($("div").id("file-count").getText());
    }

    private UploadElement getUpload() {
        return $(UploadElement.class).id("test-upload");
    }

}
