/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;

import com.vaadin.flow.demo.ComponentDemoTest;

import static org.junit.Assert.assertThat;

/**
 * Upload component test class.
 */
public class UploadIT extends ComponentDemoTest {

    @Test
    public void testUploadAnyFile() throws Exception {
        open();

        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();
        fillPathToUploadInput(tempFile.getPath());

        WebElement uploadOutput = getDriver().findElement(By.id("test-output"));
        String actualFileName = uploadOutput.findElement(By.tagName("p"))
                .getText();
        Assert.assertEquals("File name was wrong.", tempFile.getName(),
                actualFileName);

        String content = uploadOutput.getText();

        String expectedContent = actualFileName + "\n" + getTempFileContents();

        Assert.assertEquals("Upload content does not match expected",
                expectedContent, content);
    }

    @Test
    public void uploadFileAndNoErrorThrown() throws Exception {
        open();
        waitUntil(driver -> getUpload().isDisplayed());

        File tempFile = createTempFile();
        fillPathToUploadInput(tempFile.getPath());

        List<LogEntry> logList1 = getLogEntries(Level.SEVERE);
        assertThat("There should have no severe message in the console",
                logList1.size(), CoreMatchers.is(0));

        WebElement upload = getUpload();
        executeScript("arguments[0]._removeFile()", upload);
        List<LogEntry> logList2 = getLogEntries(Level.SEVERE);
        assertThat("There should have no severe message in the console",
                logList2.size(), CoreMatchers.is(0));
    }

    @Test
    public void i18nUploadTest() {
        open();
        waitUntil(driver -> getUpload().isDisplayed());

        WebElement upload = findElement(By.id("i18n-upload"));
        WebElement dropLabel = findInShadowRoot(upload, By.id("dropLabel"))
                .get(0);
        Assert.assertEquals("Перетащите файл сюда...", dropLabel.getText());
    }

    /**
     * @return The generated temp file handle
     * @throws IOException
     */
    private File createTempFile() throws IOException {
        File tempFile = File.createTempFile("TestFileUpload", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(getTempFileContents());
        writer.close();
        tempFile.deleteOnExit();
        return tempFile;
    }

    private String getTempFileContents() {
        return "This is a test file! Row 2 Row3";
    }

    private void fillPathToUploadInput(String tempFileName) throws Exception {
        // create a valid path in upload input element. Instead of selecting a
        // file by some file browsing dialog, we use the local path directly.
        WebElement input = getInput();
        setLocalFileDetector(input);
        input.sendKeys(tempFileName);
    }

    private WebElement getUpload() {
        return getDriver().findElement(By.id("test-upload"));
    }

    /**
     * Get the web component for the actual upload button hidden in the upload
     * component.
     *
     * @return actual upload button
     */
    private WebElement getInput() {
        return getInShadowRoot(getUpload(), By.id("fileInput"));
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

    @Override
    protected String getTestPath() {
        return "/vaadin-upload";
    }
}
