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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-upload-with-handler")
public class UploadWithHandlerIT extends AbstractUploadIT {

    // The size is made 100KB to be bigger than the default upload progress
    // interval
    private static final int FILE_SIZE = 1024 * 100;
    private WebElement uploadOutput;
    private WebElement eventsOutput;

    @Before
    public void init() {
        open();
        waitUntil(driver -> getUpload().isDisplayed());
        uploadOutput = getDriver().findElement(
                By.id(UploadWithHandlerView.UPLOAD_TEST_CONTENT_ID));
        eventsOutput = getDriver().findElement(
                By.id(UploadWithHandlerView.UPLOAD_HANDLER_EVENTS_ID));
    }

    @Test
    public void uploadWithHandler_uploadSingleFile_fileUploadedAndNoErrorThrown()
            throws Exception {
        File tempFile = createTempBinaryFile();
        getUpload().upload(tempFile);

        List<LogEntry> logList = getLogEntries(Level.SEVERE);
        MatcherAssert.assertThat(
                "There should have no severe message in the console for Upload with Upload Handler",
                logList.size(), CoreMatchers.is(0));

        Assert.assertEquals(
                "Unexpected handler events for upload with using upload handler",
                "started-progress-completed", eventsOutput.getText());

        Assert.assertEquals(
                "Unexpected file size for upload with upload handler",
                uploadOutput.getText(), String.valueOf(FILE_SIZE));
    }

    private UploadElement getUpload() {
        return $(UploadElement.class).id(UploadWithHandlerView.UPLOAD_ID);
    }

    private File createTempBinaryFile() throws IOException {
        File tempFile = File.createTempFile("TestFileUpload", "");

        byte[] bytes = new byte[FILE_SIZE];
        new java.util.Random().nextBytes(bytes);

        try (FileOutputStream fos = new FileOutputStream(tempFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(bytes);
        }

        return tempFile;
    }

}
