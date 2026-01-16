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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.safari.SafariDriver;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-upload-button&gt;</code>
 * element.
 */
@Element("vaadin-upload-button")
public class UploadButtonElement extends TestBenchElement {

    /**
     * Uploads the given local file using this button. Waits for 60 seconds for
     * the upload to finish.
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
     * Uploads the given local file using this button and waits for the given
     * number of seconds for the upload to finish.
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
        TestBenchElement uploadElement;
        if (isLocalDriver(getDriver())) {
            uploadElement = getFileInput();
        } else {
            uploadElement = setLocalFileDetector();
        }

        // Element must be focusable for Edge and Firefox
        Boolean hidden = uploadElement.getPropertyBoolean("hidden");
        uploadElement.setProperty("hidden", false);

        try {
            // Firefox uploads the previous file again without this
            uploadElement.clear();
        } catch (Exception e) {
            // Edge throws "InvalidElementStateException: The element is not
            // editable"
        }

        uploadElement.sendKeys(file.getPath());
        uploadElement.setProperty("hidden", hidden);

        if (maxSeconds > 0) {
            waitForUploads(maxSeconds);
        }
    }

    /**
     * Wait for the given number of seconds for all uploads to finish.
     *
     * @param maxSeconds
     *            the number of seconds to wait for the upload to finish
     */
    private void waitForUploads(int maxSeconds) {
        String script = "return !arguments[0].manager || arguments[0].manager.files.every((file) => !file.uploading);";

        waitUntil(driver -> (Boolean) executeScript(script,
                UploadButtonElement.this), maxSeconds);
    }

    private TestBenchElement setLocalFileDetector() {
        TestBenchElement uploadElement = getFileInput();

        WebElement realUploadElement = uploadElement;
        while (realUploadElement instanceof WrapsElement) {
            realUploadElement = ((WrapsElement) realUploadElement)
                    .getWrappedElement();
        }
        if (realUploadElement instanceof RemoteWebElement) {
            ((RemoteWebElement) realUploadElement)
                    .setFileDetector(new LocalFileDetector());
        } else {
            throw new IllegalArgumentException("Expected argument of type "
                    + RemoteWebElement.class.getName() + ", received "
                    + realUploadElement.getClass().getName());
        }

        return uploadElement;
    }

    private TestBenchElement getFileInput() {
        return getPropertyElement("$", "fileInput");
    }

    private static boolean isLocalDriver(WebDriver driver) {
        while (driver instanceof WrapsDriver) {
            driver = ((WrapsDriver) driver).getWrappedDriver();
        }
        return driver instanceof ChromiumDriver
                || driver instanceof FirefoxDriver
                || driver instanceof SafariDriver;
    }
}
