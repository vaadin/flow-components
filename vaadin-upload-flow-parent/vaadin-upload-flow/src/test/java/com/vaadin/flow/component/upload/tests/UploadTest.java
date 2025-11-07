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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.Upload.UploadFormat;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

public class UploadTest {

    @Test
    public void uploadNewUpload() {
        // Test no NPE due missing UI when setAttribute is called.
        Upload upload = new Upload();
    }

    @Test
    public void implementsHasEnabled() {
        Assert.assertTrue(HasEnabled.class.isAssignableFrom(Upload.class));
    }

    @Test
    public void switchBetweenSingleAndMultiFileReceiver_assertMaxFilesProperty() {
        Upload upload = new Upload();

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));

        upload.setReceiver(new MultiFileMemoryBuffer());
        Assert.assertNull(upload.getElement().getProperty("maxFiles"));

        upload.setReceiver(new MemoryBuffer());
        Assert.assertEquals(1, upload.getElement().getProperty("maxFiles", 0));
    }

    @Test
    public void uploadFormatDefaultsToRaw() {
        Upload upload = new Upload();
        Assert.assertEquals("Upload format should default to RAW",
                UploadFormat.RAW, upload.getUploadFormat());
    }

    @Test
    public void setUploadFormatToMultipart() {
        Upload upload = new Upload();
        upload.setUploadFormat(UploadFormat.MULTIPART);
        Assert.assertEquals("Upload format should be MULTIPART",
                UploadFormat.MULTIPART, upload.getUploadFormat());
        Assert.assertEquals("multipart",
                upload.getElement().getProperty("uploadFormat"));
    }

    @Test
    public void setUploadFormatToRaw() {
        Upload upload = new Upload();
        upload.setUploadFormat(UploadFormat.MULTIPART);
        upload.setUploadFormat(UploadFormat.RAW);
        Assert.assertEquals("Upload format should be RAW", UploadFormat.RAW,
                upload.getUploadFormat());
        Assert.assertEquals("raw",
                upload.getElement().getProperty("uploadFormat"));
    }

    @Test(expected = NullPointerException.class)
    public void setUploadFormatToNull_throwsException() {
        Upload upload = new Upload();
        upload.setUploadFormat(null);
    }
}
