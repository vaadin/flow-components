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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

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

        File tempFile = createTempFile("txt");
        upload.upload(tempFile);

        WebElement uploadOutput = getDriver()
                .findElement(By.id("single-upload-output"));

        String content = uploadOutput.getText();

        String expectedContent = tempFile.getName() + getTempFileContents();

        Assert.assertEquals("Upload content does not match expected",
                expectedContent, content);
    }

    @Test
    public void testUploadMultipleEventOrder() throws Exception {
        open();

        final UploadElement upload = $(UploadElement.class).id("multi-upload");
        waitUntil(driver -> upload.isDisplayed());

        File tempFile = createTempFile("txt");

        upload.uploadMultiple(List.of(tempFile, tempFile, tempFile), 10);

        WebElement eventsOutput = getDriver()
                .findElement(By.id("multi-upload-event-output"));

        Assert.assertEquals("Upload event order does not match expected",
                "-succeeded-succeeded-succeeded-finished",
                eventsOutput.getText());
    }
}
