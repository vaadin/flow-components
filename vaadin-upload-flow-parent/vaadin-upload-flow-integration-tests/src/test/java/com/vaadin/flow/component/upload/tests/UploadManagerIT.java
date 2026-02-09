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
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;

import com.vaadin.flow.component.upload.testbench.UploadButtonElement;
import com.vaadin.flow.component.upload.testbench.UploadFileListElement;
import com.vaadin.flow.component.upload.testbench.UploadManagerTester;
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

    @Test
    public void reattachOwner_setMaxFiles_maxFilesIsEnforced()
            throws Exception {
        // Detach the owner component
        clickButton("detach-owner");
        assertLogContains("Owner detached");

        // Reattach the owner component
        clickButton("reattach-owner");
        assertLogContains("Owner reattached");

        // Set max files limit after reattach
        clickButton("set-max-files-1");

        // Try to upload two files - second should be rejected
        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");

        uploadFiles(tempFile1, tempFile2);

        // Verify that max files constraint is enforced
        assertLogContains("Rejected:");
    }

    @Test
    public void unlinkButton_uploadFails() throws Exception {
        // Unlink the upload button from the manager
        clickButton("unlink-button");
        assertLogContains("Upload button unlinked");

        File tempFile = createTempFile("txt");

        // Try to upload - should fail since button is unlinked
        // The manager property is null after unlinking, so calling addFiles
        // will throw a JavascriptException
        UploadButtonElement uploadButton = $(UploadButtonElement.class)
                .id("upload-button");
        var tester = uploadButton.getUploadManager();
        try {
            tester.upload(tempFile);
            Assert.fail("Expected JavascriptException when uploading "
                    + "with unlinked button");
        } catch (JavascriptException e) {
            // Expected - manager is null after unlinking
            Assert.assertTrue("Exception should indicate manager is null",
                    e.getMessage().contains("null"));
        }
    }

    @Test
    public void getMaxFiles_defaultIsInfinity() {
        // Log status first to ensure the manager is initialized
        logStatus();
        assertLogContains("maxFiles=0");

        UploadManagerTester tester = getUploadManagerTester();
        Assert.assertTrue("Default maxFiles should be Infinity",
                Double.isInfinite(tester.getMaxFiles()));
    }

    @Test
    public void getMaxFiles_returnsConfiguredValue() {
        clickButton("set-max-files-3");

        UploadManagerTester tester = getUploadManagerTester();
        Assert.assertEquals("maxFiles should be 3", 3.0, tester.getMaxFiles(),
                0.0);
    }

    @Test
    public void isMaxFilesReached_falseWhenNoFiles() {
        clickButton("set-max-files-1");

        UploadManagerTester tester = getUploadManagerTester();
        Assert.assertFalse("maxFilesReached should be false with no files",
                tester.isMaxFilesReached());
    }

    @Test
    public void isMaxFilesReached_trueWhenLimitReached() throws Exception {
        clickButton("set-max-files-1");

        File tempFile = createTempFile("txt");
        uploadFile(tempFile);
        assertLogContains("Uploaded:");

        UploadManagerTester tester = getUploadManagerTester();
        Assert.assertTrue("maxFilesReached should be true after uploading",
                tester.isMaxFilesReached());
    }

    @Test
    public void removeFile_removesFileAtIndex() throws Exception {
        File tempFile = createTempFile("txt");
        uploadFile(tempFile);
        assertLogContains("Uploaded:");

        UploadManagerTester tester = getUploadManagerTester();
        waitUntil(driver -> tester.getFileCount() > 0, 10);
        Assert.assertEquals("File count should be 1", 1, tester.getFileCount());

        tester.removeFile(0);
        assertLogContains("Removed: " + tempFile.getName());
        Assert.assertEquals("File count should be 0 after removal", 0,
                tester.getFileCount());
    }

    @Test
    public void uploadFiles_triggersManualUpload() throws Exception {
        clickButton("disable-auto-upload");

        File tempFile = createTempFile("txt");

        // Add file without triggering upload
        UploadManagerTester tester = getUploadManagerTester();
        tester.upload(tempFile, 0);

        // Verify file was added but not uploaded
        waitUntil(driver -> tester.getFileCount() > 0, 10);
        Thread.sleep(500); // NOSONAR
        logStatus();
        Assert.assertFalse("File should not be uploaded automatically",
                getLogText().contains("Uploaded:"));

        // Trigger manual upload
        tester.uploadFiles();
        tester.waitForUploads(60);

        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void uploadMultiple_uploadsAllFiles() throws Exception {
        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");
        File tempFile3 = createTempFile("file3", "txt");

        UploadManagerTester tester = getUploadManagerTester();
        tester.uploadMultiple(Arrays.asList(tempFile1, tempFile2, tempFile3),
                60);

        assertLogContains("All uploads finished");
        assertLogContains("Uploaded: " + tempFile1.getName());
        assertLogContains("Uploaded: " + tempFile2.getName());
        assertLogContains("Uploaded: " + tempFile3.getName());
    }

    @Test
    public void abort_canBeCalledWithoutError() throws Exception {
        // Disable auto-upload so we can control the timing
        clickButton("disable-auto-upload");

        File tempFile = createTempFile("txt");

        UploadManagerTester tester = getUploadManagerTester();

        // Add file without triggering upload
        tester.upload(tempFile, 0);

        waitUntil(driver -> tester.getFileCount() == 1, 10);

        // Call abort - should not throw error even with no active uploads
        tester.abort();

        // Verify the file is still in the list (not uploaded, not removed)
        Assert.assertEquals("File should still be in the list", 1,
                tester.getFileCount());

        // Now trigger the upload and verify it works
        tester.uploadFiles();
        tester.waitForUploads(60);

        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void getFileCount_returnsCorrectCount() throws Exception {
        UploadManagerTester tester = getUploadManagerTester();
        Assert.assertEquals("Initial file count should be 0", 0,
                tester.getFileCount());

        File tempFile1 = createTempFile("file1", "txt");
        File tempFile2 = createTempFile("file2", "txt");

        uploadFile(tempFile1);
        assertLogContains("Uploaded: " + tempFile1.getName());
        Assert.assertEquals("File count should be 1", 1, tester.getFileCount());

        uploadFile(tempFile2);
        assertLogContains("Uploaded: " + tempFile2.getName());
        Assert.assertEquals("File count should be 2", 2, tester.getFileCount());
    }

    @Test
    public void waitForUploads_waitsUntilComplete() throws Exception {
        File tempFile = createTempFile("txt");

        UploadManagerTester tester = getUploadManagerTester();
        tester.upload(tempFile, 0);
        tester.waitForUploads(60);

        // If we got here without timeout, the wait worked correctly
        assertLogContains("Uploaded: " + tempFile.getName());
    }

    @Test
    public void allFinishedEvent_firesWithoutAdditionalRoundtrip()
            throws Exception {
        File tempFile = createTempFile("txt");

        UploadManagerTester tester = getUploadManagerTester();
        tester.upload(tempFile, 0);
        tester.waitForUploads(60);

        // Wait for "All uploads finished" message WITHOUT triggering any
        // additional server roundtrip (e.g., no button clicks).
        // This verifies that the allFinished event properly triggers a UI
        // update from the client-side event, not requiring Push or additional
        // requests.
        waitUntil(driver -> getLogText().contains("All uploads finished"), 10);
    }

    private UploadManagerTester getUploadManagerTester() {
        return $(UploadButtonElement.class).id("upload-button")
                .getUploadManager();
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
