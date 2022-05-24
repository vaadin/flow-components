/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

import static org.junit.Assert.assertThat;

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
