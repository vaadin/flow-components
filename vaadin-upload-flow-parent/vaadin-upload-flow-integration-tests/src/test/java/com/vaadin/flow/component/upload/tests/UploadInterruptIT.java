/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-upload/interrupt")
public class UploadInterruptIT extends AbstractUploadIT {

    private UploadElement upload;
    private WebElement uploadOutput;
    private WebElement eventsOutput;

    @Before
    public void init() {
        open();
        upload = $(UploadElement.class).waitForFirst();
        uploadOutput = $("div").id("test-output");
        eventsOutput = $("div").id("test-events-output");
    }

    @Test
    public void uploadMultipleFiles_interruptUpload_allOngoingUploadsInterrupted()
            throws Exception {

        List<File> files = new ArrayList<>();
        files.add(createTempFile("txt"));
        files.add(createTempFile("txt"));
        files.add(createTempFile("interrupt.txt"));
        files.add(createTempFile("txt"));
        files.add(createTempFile("txt"));

        upload.setProperty("noAuto", true);
        files.forEach(file -> upload.upload(file, 0));
        executeScript("arguments[0].uploadFiles()", upload);
        waitForUploads();

        Assert.assertEquals("Expected all uploads to be interrupted",
                "-failed-failed-failed-failed-failed-finished",
                eventsOutput.getText());

        files.forEach(file -> Assert.assertTrue(
                "Expected upload of " + file.getName() + " to be failed",
                uploadOutput.getText().contains(
                        "FAILED:" + file.getName() + ",Upload interrupted")));
    }

    private void waitForUploads() {
        String script = "return arguments[0].files.every((file) => !file.uploading);";

        waitUntil(driver -> (Boolean) executeScript(script, upload), 10);
    }
}
