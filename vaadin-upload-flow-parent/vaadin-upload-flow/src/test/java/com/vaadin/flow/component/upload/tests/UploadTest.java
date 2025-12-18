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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadDropZone;
import com.vaadin.flow.component.upload.UploadFileList;
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
    public void setHeadless() {
        Upload upload = new Upload();
        Assert.assertFalse(upload.isHeadless());

        upload.setHeadless(true);
        Assert.assertTrue(upload.isHeadless());
        Assert.assertTrue(upload.getElement().getProperty("headless", false));

        upload.setHeadless(false);
        Assert.assertFalse(upload.isHeadless());
    }

    @Test
    public void setAddButton() {
        Upload upload = new Upload();
        Assert.assertNull(upload.getAddButton());

        Div button = new Div();
        upload.setAddButton(button);
        Assert.assertEquals(button, upload.getAddButton());

        upload.setAddButton(null);
        Assert.assertNull(upload.getAddButton());
    }

    @Test
    public void setDropZone() {
        Upload upload = new Upload();
        Assert.assertNull(upload.getDropZone());

        UploadDropZone dropZone = new UploadDropZone();
        upload.setDropZone(dropZone);
        Assert.assertEquals(dropZone, upload.getDropZone());

        upload.setDropZone(null);
        Assert.assertNull(upload.getDropZone());
    }

    @Test
    public void setFileList() {
        Upload upload = new Upload();
        Assert.assertNull(upload.getFileList());

        UploadFileList fileList = new UploadFileList();
        upload.setFileList(fileList);
        Assert.assertEquals(fileList, upload.getFileList());

        upload.setFileList(null);
        Assert.assertNull(upload.getFileList());
    }
}
