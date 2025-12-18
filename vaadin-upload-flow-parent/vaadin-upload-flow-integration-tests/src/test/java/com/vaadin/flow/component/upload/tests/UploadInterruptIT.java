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

        List<File> files = List.of(createTempFile("txt"), createTempFile("txt"),
                createTempFile("interrupt.txt"), createTempFile("txt"),
                createTempFile("txt"));

        upload.uploadMultiple(files, 10);

        Assert.assertEquals("Expected all uploads to be interrupted",
                "-failed-failed-failed-failed-failed-finished",
                eventsOutput.getText());

        files.forEach(file -> Assert.assertTrue(
                "Expected upload of " + file.getName() + " to be failed",
                uploadOutput.getText().contains(
                        "FAILED:" + file.getName() + ",Upload interrupted")));
    }
}
