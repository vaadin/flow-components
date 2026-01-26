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

import com.vaadin.flow.component.upload.testbench.UploadButtonElement;
import com.vaadin.flow.component.upload.testbench.UploadFileListElement;
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
        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void uploadMultipleFiles_allFilesAreUploaded() throws Exception {
        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");

        uploadFiles(tempFile1, tempFile2);
        assertLogContains("All uploads finished");
        assertLogContains("Uploaded: " + tempFile1.getName());
        assertLogContains("Uploaded: " + tempFile2.getName());
    }

    @Test
    public void setMaxFiles_exceedLimit_fileIsRejected() throws Exception {
        clickButton("set-max-files-1");

        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");

        uploadFiles(tempFile1, tempFile2);
        assertLogContains("Rejected:");
    }

    @Test
    public void setMaxFileSize_exceedLimit_fileIsRejected() throws Exception {
        clickButton("set-max-file-size-100");

        // Create a file larger than 100 bytes
        File largeFile = createLargeFile(150);

        uploadFile(largeFile);
        assertLogContains("Rejected: " + largeFile.getName());
    }

    @Test
    public void setAcceptedFileTypes_wrongType_fileIsRejected()
            throws Exception {
        clickButton("set-accept-image");

        File textFile = createTempFile("txt");

        uploadFile(textFile);
        assertLogContains("Rejected: " + textFile.getName());
    }

    @Test
    public void setAcceptedFileTypes_correctType_fileIsUploaded()
            throws Exception {
        clickButton("set-accept-text");

        File textFile = createTempFile("txt");

        uploadFile(textFile);
        assertLogContains("Uploaded: " + textFile.getName());
    }

    @Test
    public void disableAutoUpload_fileIsNotUploadedAutomatically()
            throws Exception {
        clickButton("disable-auto-upload");

        File tempFile = createTempFile("txt");

        // Add file without triggering upload
        uploadFile(tempFile);

        // Wait a bit to make sure no upload happens
        Thread.sleep(500); // NOSONAR
        logStatus();

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
        uploadFile(tempFile);

        // Click the start button on the first file in the list
        UploadFileListElement fileList = $(UploadFileListElement.class)
                .id("file-list");
        waitUntil(driver -> fileList.getUploadManager().getFileCount() > 0, 10);
        fileList.$("vaadin-upload-file").first().$("[part~='start-button']")
                .first().click();

        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void removeFile_fileRemovedEventIsFired() throws Exception {
        File tempFile = createTempFile("txt");

        uploadFile(tempFile);

        assertLogContains("Uploaded:");

        // Click the remove button on the first file in the list
        UploadFileListElement fileList = $(UploadFileListElement.class)
                .id("file-list");
        waitUntil(driver -> fileList.getUploadManager().getFileCount() > 0, 10);
        fileList.$("vaadin-upload-file").first().$("[part~='remove-button']")
                .first().click();

        assertLogContains("Removed: " + tempFile.getName());
    }

    @Test
    public void clearFileList_filesAreCleared() throws Exception {
        File tempFile = createTempFile("txt");

        uploadFile(tempFile);
        logStatus();
        assertLogContains("Uploaded:");

        UploadFileListElement fileList = $(UploadFileListElement.class)
                .id("file-list");
        Assert.assertEquals("File count should be 1", 1,
                fileList.getUploadManager().getFileCount());

        clickButton("clear-file-list");

        Assert.assertEquals("File count should be 0 after clearing", 0,
                fileList.getUploadManager().getFileCount());
    }

    @Test
    public void disableManager_uploadFails() throws Exception {
        clickButton("disable-manager");

        File tempFile = createTempFile("txt");
        uploadFile(tempFile);

        // Wait a bit for any potential upload
        Thread.sleep(500); // NOSONAR
        logStatus();

        // Verify no upload happened when manager is disabled
        Assert.assertFalse("Upload should fail when manager is disabled",
                getLogText().contains("Uploaded:"));
    }

    @Test
    public void detachOwner_uploadFails() throws Exception {
        // Detach the owner component
        clickButton("detach-owner");
        assertLogContains("Owner detached");

        File tempFile = createTempFile("txt");

        // Try to upload - should fail since owner is detached
        uploadFile(tempFile);

        // Wait a bit for any potential upload to complete
        Thread.sleep(1000); // NOSONAR
        logStatus();

        // Verify no upload happened
        Assert.assertFalse("Upload should fail when owner is detached",
                getLogText().contains("Uploaded:"));
    }

    private void uploadFile(File file) {
        UploadButtonElement uploadButton = $(UploadButtonElement.class)
                .id("upload-button");
        uploadButton.getUploadManager().upload(file);
    }

    private void uploadFiles(File... files) {
        UploadButtonElement uploadButton = $(UploadButtonElement.class)
                .id("upload-button");
        for (File file : files) {
            uploadButton.getUploadManager().upload(file, 0);
        }
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }

    private String getLogText() {
        return findElement(By.id("log-area")).getText();
    }

    private void assertLogContains(String text) {
        waitUntil(driver -> {
            logStatus();
            return getLogText().contains(text);
        }, 100);
    }

    private void logStatus() {
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
