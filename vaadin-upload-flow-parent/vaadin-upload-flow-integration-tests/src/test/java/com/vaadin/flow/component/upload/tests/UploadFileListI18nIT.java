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

import com.vaadin.flow.component.upload.testbench.UploadButtonElement;
import com.vaadin.flow.component.upload.testbench.UploadFileListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-upload/file-list-i18n")
public class UploadFileListI18nIT extends AbstractUploadIT {

    private UploadButtonElement uploadButton;
    private UploadFileListElement fileList;

    @Before
    public void init() {
        open();
        uploadButton = $(UploadButtonElement.class).waitForFirst();
        fileList = $(UploadFileListElement.class).waitForFirst();
    }

    @Test
    public void setI18n_i18nIsApplied() throws Exception {
        clickElementWithJs("set-i18n");

        File tempFile = createTempFile("txt");
        uploadButton.getUploadManager().upload(tempFile, 0);

        TestBenchElement uploadFile = getUploadFile(fileList);

        Assert.assertEquals(
                UploadFileListI18nPage.FULL_I18N.getFile().getRemove(),
                getButtonAriaLabel(uploadFile, "remove-button"));
        Assert.assertEquals(
                UploadFileListI18nPage.FULL_I18N.getFile().getStart(),
                getButtonAriaLabel(uploadFile, "start-button"));
        Assert.assertEquals(UploadFileListI18nPage.FULL_I18N.getUploading()
                .getStatus().getHeld(), getStatusText(uploadFile));
    }

    @Test
    public void setI18n_setEmptyI18n_defaultI18nIsRestored() throws Exception {
        clickElementWithJs("set-i18n");
        clickElementWithJs("set-empty-i18n");

        File tempFile = createTempFile("txt");
        uploadButton.getUploadManager().upload(tempFile, 0);

        TestBenchElement uploadFile = getUploadFile(fileList);

        Assert.assertEquals("Remove",
                getButtonAriaLabel(uploadFile, "remove-button"));
        Assert.assertEquals("Start",
                getButtonAriaLabel(uploadFile, "start-button"));
        Assert.assertEquals("Queued", getStatusText(uploadFile));
    }

    private TestBenchElement getUploadFile(UploadFileListElement fileList) {
        return fileList.$("vaadin-upload-file").first();
    }

    private String getButtonAriaLabel(TestBenchElement uploadFile,
            String buttonPart) {
        return uploadFile.$("[part~='" + buttonPart + "']").first()
                .getAttribute("aria-label");
    }

    private String getStatusText(TestBenchElement uploadFile) {
        return uploadFile.$("[part~='status']").first().getText();
    }
}
