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
package com.vaadin.flow.component.upload.testbench;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.testbench.TestBenchElement;

/**
 * A test helper class for interacting with the client-side UploadManager. This
 * class provides access to the upload manager's state and methods through any
 * element that is linked to the manager.
 * <p>
 * Obtain an instance through {@link UploadButtonElement#getUploadManager()},
 * {@link UploadDropZoneElement#getUploadManager()}, or
 * {@link UploadFileListElement#getUploadManager()}.
 */
public class UploadManagerTester {

    private final TestBenchElement element;

    /**
     * Creates a new tester for the upload manager linked to the given element.
     *
     * @param element
     *            an element linked to an upload manager
     */
    UploadManagerTester(TestBenchElement element) {
        this.element = element;
    }

    /**
     * Gets the maximum number of files that can be uploaded.
     *
     * @return the maximum number of files, or {@code Infinity} if unlimited
     */
    public double getMaxFiles() {
        Object result = executeScript("return arguments[0].manager.maxFiles");
        if (result == null) {
            return Double.POSITIVE_INFINITY;
        }
        return ((Number) result).doubleValue();
    }

    /**
     * Checks whether the maximum number of files has been reached.
     *
     * @return {@code true} if no more files can be uploaded, {@code false}
     *         otherwise
     */
    public boolean isMaxFilesReached() {
        return (boolean) executeScript(
                "return arguments[0].manager.maxFilesReached");
    }

    /**
     * Gets the number of files currently in the upload list.
     *
     * @return the number of files
     */
    public int getFileCount() {
        return ((Number) executeScript(
                "return arguments[0].manager.files.length")).intValue();
    }

    /**
     * Removes the file at the given index.
     *
     * @param index
     *            the index of the file to remove
     */
    public void removeFile(int index) {
        executeScript(
                "const file = arguments[0].manager.files[arguments[1]];"
                        + "if (file) arguments[0].manager.removeFile(file);",
                index);
    }

    /**
     * Starts uploading all pending files. This is useful when auto-upload is
     * disabled.
     */
    public void uploadFiles() {
        executeScript("arguments[0].manager.uploadFiles()");
    }

    /**
     * Aborts all uploads currently in progress.
     */
    public void abort() {
        executeScript(
                "arguments[0].manager.files.forEach(f => { if (f.uploading) arguments[0].manager.abortUpload(f); })");
    }

    /**
     * Waits for all uploads to complete.
     *
     * @param maxSeconds
     *            the maximum number of seconds to wait
     */
    public void waitForUploads(int maxSeconds) {
        new WebDriverWait(element.getDriver(), Duration.ofSeconds(maxSeconds))
                .until(driver -> (boolean) executeScript(
                        "return arguments[0].manager.files.every(f => !f.uploading)"));
    }

    /**
     * Uploads the given local file. Waits for 60 seconds for the upload to
     * finish.
     * <p>
     * Note that Safari webdriver does not support file uploads.
     *
     * @param file
     *            a reference to the local file to upload
     */
    public void upload(File file) {
        upload(file, 60);
    }

    /**
     * Uploads the given local file and waits for the given number of seconds
     * for the upload to finish.
     * <p>
     * Note that Safari webdriver does not support file uploads.
     *
     * @param file
     *            the local file to upload
     * @param maxSeconds
     *            the number of seconds to wait for the upload to finish or
     *            {@code 0} not to wait
     */
    public void upload(File file, int maxSeconds) {
        // Create a temporary file input element
        TestBenchElement fileInput = (TestBenchElement) executeScript(
                "const input = document.createElement('input');"
                        + "input.type = 'file';"
                        + "input.style.position = 'absolute';"
                        + "input.style.left = '-9999px';"
                        + "document.body.appendChild(input);"
                        + "return input;");

        // Set up local file detector for remote drivers
        if (!isLocalDriver(element.getDriver())) {
            setLocalFileDetector(fileInput);
        }

        // Send the file path to the input
        fileInput.sendKeys(file.getPath());

        // Pass the files to the manager and clean up the input
        executeScript("arguments[0].manager.addFiles(arguments[1].files);"
                + "arguments[1].remove();", fileInput);

        if (maxSeconds > 0) {
            waitForUploads(maxSeconds);
        }
    }

    /**
     * Uploads the given local files and waits for the given number of seconds
     * for all uploads to finish.
     * <p>
     * Note that Safari webdriver does not support file uploads.
     *
     * @param files
     *            the local files to upload
     * @param maxSeconds
     *            the number of seconds to wait for uploads to finish or
     *            {@code 0} not to wait
     */
    public void uploadMultiple(List<File> files, int maxSeconds) {
        for (File file : files) {
            upload(file, 0);
        }
        if (maxSeconds > 0) {
            waitForUploads(maxSeconds);
        }
    }

    private void setLocalFileDetector(TestBenchElement fileInput) {
        WebElement realElement = fileInput;
        while (realElement instanceof WrapsElement wrapsElement) {
            realElement = wrapsElement.getWrappedElement();
        }
        if (realElement instanceof RemoteWebElement remoteWebElement) {
            remoteWebElement.setFileDetector(new LocalFileDetector());
        }
    }

    private static boolean isLocalDriver(WebDriver driver) {
        while (driver instanceof WrapsDriver wrapsDriver) {
            driver = wrapsDriver.getWrappedDriver();
        }
        return driver instanceof ChromiumDriver
                || driver instanceof FirefoxDriver
                || driver instanceof SafariDriver;
    }

    private Object executeScript(String script, Object... args) {
        Object[] allArgs = new Object[args.length + 1];
        allArgs[0] = element;
        System.arraycopy(args, 0, allArgs, 1, args.length);
        return element.getCommandExecutor().executeScript(script, allArgs);
    }
}
