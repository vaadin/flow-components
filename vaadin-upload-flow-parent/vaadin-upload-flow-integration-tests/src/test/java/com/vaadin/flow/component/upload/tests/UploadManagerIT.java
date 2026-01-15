/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

/**
 * Integration tests for {@link com.vaadin.flow.component.upload.UploadManager}.
 */
@TestPath("vaadin-upload/manager")
public class UploadManagerIT extends AbstractUploadIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("log-area"));
    }

    @Test
    public void uploadFile_fileIsUploaded() throws Exception {
        File tempFile = createTempFile("txt");

        uploadFile(tempFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void uploadFile_allFinishedEventIsFired() throws Exception {
        File tempFile = createTempFile("txt");

        uploadFile(tempFile);
        triggerServerRoundtrip();
        waitForLog("All uploads finished");

        assertLogContains("All uploads finished");
    }

    @Test
    public void uploadMultipleFiles_allFilesAreUploaded() throws Exception {
        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");

        uploadFiles(tempFile1, tempFile2);
        triggerServerRoundtrip();
        waitForLog("All uploads finished");

        assertLogContains("Uploaded: " + tempFile1.getName());
        assertLogContains("Uploaded: " + tempFile2.getName());
        assertLogContains("All uploads finished");
    }

    @Test
    public void setMaxFiles_exceedLimit_fileIsRejected() throws Exception {
        clickButton("set-max-files-1");

        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");

        uploadFiles(tempFile1, tempFile2);
        triggerServerRoundtrip();
        waitForLog("Rejected:");

        assertLogContains("Rejected:");
    }

    @Test
    public void setMaxFileSize_exceedLimit_fileIsRejected() throws Exception {
        clickButton("set-max-file-size-100");

        // Create a file larger than 100 bytes
        File largeFile = createLargeFile(150);

        uploadFile(largeFile);
        triggerServerRoundtrip();
        waitForLog("Rejected:");

        assertLogContains("Rejected: " + largeFile.getName());
    }

    @Test
    public void setAcceptedFileTypes_wrongType_fileIsRejected()
            throws Exception {
        clickButton("set-accept-image");

        File textFile = createTempFile("txt");

        uploadFile(textFile);
        triggerServerRoundtrip();
        waitForLog("Rejected:");

        assertLogContains("Rejected: " + textFile.getName());
    }

    @Test
    public void setAcceptedFileTypes_correctType_fileIsUploaded()
            throws Exception {
        clickButton("set-accept-text");

        File textFile = createTempFile("txt");

        uploadFile(textFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        assertLogContains("Uploaded: " + textFile.getName());
    }

    @Test
    public void clearAcceptedFileTypes_anyTypeIsAccepted() throws Exception {
        // First set restriction
        clickButton("set-accept-image");
        // Then clear it
        clickButton("clear-accept");

        File textFile = createTempFile("txt");

        uploadFile(textFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        assertLogContains("Uploaded: " + textFile.getName());
    }

    @Test
    public void disableAutoUpload_fileIsNotUploadedAutomatically()
            throws Exception {
        clickButton("disable-auto-upload");

        File tempFile = createTempFile("txt");

        // Add file without triggering upload
        setFileInputValue(tempFile);

        // Wait a bit to make sure no upload happens
        Thread.sleep(500);

        // Verify no upload happened
        Assert.assertFalse("File should not be uploaded automatically",
                getLogText().contains("Uploaded:"));
    }

    @Test
    public void disableAutoUpload_triggerUpload_fileIsUploaded()
            throws Exception {
        clickButton("disable-auto-upload");

        File tempFile = createTempFile("txt");

        // Add file without triggering upload
        setFileInputValue(tempFile);

        // Manually trigger upload
        clickButton("trigger-upload");
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void removeFile_fileRemovedEventIsFired() throws Exception {
        File tempFile = createTempFile("txt");

        uploadFile(tempFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        clickButton("remove-first-file");
        waitForLog("Removed:");

        assertLogContains("Removed: " + tempFile.getName());
    }

    @Test
    public void clearFileList_filesAreCleared() throws Exception {
        File tempFile = createTempFile("txt");

        uploadFile(tempFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        clickButton("get-file-count");
        waitForLog("File count:");
        assertLogContains("File count: 1");

        clickButton("clear-log");
        clickButton("clear-file-list");

        clickButton("get-file-count");
        waitForLog("File count:");
        assertLogContains("File count: 0");
    }

    @Test
    public void statusButton_logsCorrectDefaultStatus() {
        clickButton("status-button");
        waitForLog("Status:");

        String log = getLogText();
        Assert.assertTrue("Status should show enabled=true",
                log.contains("enabled=true"));
        Assert.assertTrue("Status should show uploading=false",
                log.contains("uploading=false"));
        Assert.assertTrue("Status should show interrupted=false",
                log.contains("interrupted=false"));
        Assert.assertTrue("Status should show maxFiles=0",
                log.contains("maxFiles=0"));
        Assert.assertTrue("Status should show maxFileSize=0",
                log.contains("maxFileSize=0"));
        Assert.assertTrue("Status should show autoUpload=true",
                log.contains("autoUpload=true"));
        Assert.assertTrue("Status should show acceptedTypes=[]",
                log.contains("acceptedTypes=[]"));
    }

    @Test
    public void setMaxFiles_statusReflectsChange() {
        clickButton("set-max-files-3");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("maxFiles=3");
    }

    @Test
    public void setMaxFileSize_statusReflectsChange() {
        clickButton("set-max-file-size-100");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("maxFileSize=100");
    }

    @Test
    public void setAcceptedFileTypes_statusReflectsChange() {
        clickButton("set-accept-multiple");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("acceptedTypes=[text/*, application/pdf]");
    }

    @Test
    public void disableAutoUpload_statusReflectsChange() {
        clickButton("disable-auto-upload");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("autoUpload=false");
    }

    @Test
    public void enableAutoUpload_statusReflectsChange() {
        clickButton("disable-auto-upload");
        clickButton("enable-auto-upload");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("autoUpload=true");
    }

    @Test
    public void disableManager_statusReflectsChange() {
        clickButton("disable-manager");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("enabled=false");
    }

    @Test
    public void enableManager_statusReflectsChange() {
        clickButton("disable-manager");
        clickButton("enable-manager");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("enabled=true");
    }

    @Test
    public void setMaxFilesUnlimited_statusReflectsChange() {
        clickButton("set-max-files-3");
        clickButton("set-max-files-unlimited");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("maxFiles=0");
    }

    @Test
    public void setMaxFileSizeUnlimited_statusReflectsChange() {
        clickButton("set-max-file-size-100");
        clickButton("set-max-file-size-unlimited");
        clickButton("status-button");
        waitForLog("Status:");

        assertLogContains("maxFileSize=0");
    }

    @Test
    public void uploadFileWithCorrectSize_fileIsUploaded() throws Exception {
        // Set max file size higher than the test file content
        clickButton("set-max-file-size-unlimited");

        File tempFile = createTempFile("txt");

        uploadFile(tempFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        // Verify file size is logged
        assertLogContains("bytes)");
    }

    @Test
    public void fileRejectedEvent_includesErrorMessage() throws Exception {
        clickButton("set-max-file-size-100");

        File largeFile = createLargeFile(150);

        uploadFile(largeFile);
        triggerServerRoundtrip();
        waitForLog("Rejected:");

        // Verify error message is included (file size exceeded)
        String log = getLogText();
        Assert.assertTrue("Rejection should include error message",
                log.contains("Rejected:") && log.contains(" - "));
    }

    @Test
    public void detachOwner_uploadFails() throws Exception {
        // Detach the owner component
        clickButton("detach-owner");
        waitForLog("Owner detached");

        File tempFile = createTempFile("txt");

        // Try to upload - should fail since owner is detached
        uploadFile(tempFile);

        // Wait a bit for any potential upload to complete
        Thread.sleep(1000);
        triggerServerRoundtrip();

        // Verify no upload happened
        Assert.assertFalse("Upload should fail when owner is detached",
                getLogText().contains("Uploaded:"));
    }

    @Test
    public void detachOwner_reattach_uploadWorks() throws Exception {
        // Detach the owner component
        clickButton("detach-owner");
        waitForLog("Owner detached");

        // Reattach the owner component
        clickButton("reattach-owner");
        waitForLog("Owner reattached");

        File tempFile = createTempFile("txt");

        // Upload should work again after reattachment
        uploadFile(tempFile);
        triggerServerRoundtrip();
        waitForLog("Uploaded:");

        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void detachOwner_pendingFilesNotUploaded() throws Exception {
        // Disable auto upload first
        clickButton("disable-auto-upload");

        File tempFile = createTempFile("txt");

        // Add file without triggering upload
        setFileInputValue(tempFile);

        // Detach the owner
        clickButton("detach-owner");
        waitForLog("Owner detached");

        // Try to trigger upload - should fail
        clickButton("trigger-upload");

        // Wait a bit for any potential upload
        Thread.sleep(1000);
        triggerServerRoundtrip();

        // Verify no upload happened
        Assert.assertFalse("Upload should fail when owner is detached",
                getLogText().contains("Uploaded:"));
    }

    private void uploadFile(File file) {
        WebElement input = findElement(By.id("native-file-input"));
        input.sendKeys(file.getAbsolutePath());
    }

    private void uploadFiles(File... files) {
        WebElement input = findElement(By.id("native-file-input"));
        StringBuilder paths = new StringBuilder();
        for (File file : files) {
            if (paths.length() > 0) {
                paths.append("\n");
            }
            paths.append(file.getAbsolutePath());
        }
        input.sendKeys(paths.toString());
    }

    private void setFileInputValue(File file) {
        WebElement input = findElement(By.id("native-file-input"));
        input.sendKeys(file.getAbsolutePath());
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }

    private void waitForLog(String text) {
        waitUntil(driver -> getLogText().contains(text), 10);
    }

    private String getLogText() {
        return findElement(By.id("log-area")).getText();
    }

    private void assertLogContains(String text) {
        Assert.assertTrue("Log should contain: " + text,
                getLogText().contains(text));
    }

    /**
     * Triggers a server roundtrip to fetch pending UI updates from UI.access()
     * calls. This is needed because the test app doesn't use Push.
     */
    private void triggerServerRoundtrip() {
        clickButton("status-button");
    }

    /**
     * Creates a temp file with the specified size for testing file size limits.
     */
    private File createLargeFile(int sizeInBytes) throws IOException {
        File tempFile = File.createTempFile("LargeFile", ".txt");
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(tempFile))) {
            // Write content to reach the desired file size
            StringBuilder content = new StringBuilder();
            while (content.length() < sizeInBytes) {
                content.append("X");
            }
            writer.write(content.toString());
        }
        tempFile.deleteOnExit();
        return tempFile;
    }
}
