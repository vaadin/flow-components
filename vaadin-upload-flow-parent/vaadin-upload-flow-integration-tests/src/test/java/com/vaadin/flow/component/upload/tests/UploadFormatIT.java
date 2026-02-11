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

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-upload/format")
public class UploadFormatIT extends AbstractUploadIT {

    private UploadElement upload;

    @Before
    public void init() {
        open();
        upload = $(UploadElement.class).first();
    }

    @Test
    public void uploadWithMultipartFormat_fileUploaded_multipartRequestReceived()
            throws Exception {
        File tempFile = createTempFile("txt");
        upload.upload(tempFile);

        checkLogsForErrors();

        waitUntil(driver -> getDriver().getPageSource()
                .contains("multipart=true"));
        Assert.assertTrue("Expected multipart request to be detected",
                getDriver().getPageSource().contains("multipart=true"));
    }

}
