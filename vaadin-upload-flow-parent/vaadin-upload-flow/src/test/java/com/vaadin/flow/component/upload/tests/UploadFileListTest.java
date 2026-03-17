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

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListI18N;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
class UploadFileListTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private Div owner;
    private UploadManager manager;

    @BeforeEach
    void setup() {
        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @Test
    void constructor_default_createsFileList() {
        UploadFileList fileList = new UploadFileList();

        Assertions.assertNotNull(fileList);
        Assertions.assertEquals("vaadin-upload-file-list",
                fileList.getElement().getTag());
    }

    @Test
    void constructor_withManager_linksToManager() {
        UploadFileList fileList = new UploadFileList(manager);

        Assertions.assertSame(manager, fileList.getUploadManager());
    }

    @Test
    void constructor_withNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new UploadFileList(null));
    }

    @Test
    void setUploadManager_linksToManager() {
        UploadFileList fileList = new UploadFileList();

        fileList.setUploadManager(manager);

        Assertions.assertSame(manager, fileList.getUploadManager());
    }

    @Test
    void getUploadManager_default_returnsNull() {
        UploadFileList fileList = new UploadFileList();

        Assertions.assertNull(fileList.getUploadManager());
    }

    @Test
    void getI18n_initiallyNull() {
        UploadFileList fileList = new UploadFileList();

        Assertions.assertNull(fileList.getI18n());
    }

    @Test
    void setI18n_setsI18n() {
        UploadFileList fileList = new UploadFileList();
        UploadFileListI18N i18n = new UploadFileListI18N();
        i18n.setFile(new UploadFileListI18N.File().setRetry("Retry"));

        fileList.setI18n(i18n);

        Assertions.assertSame(i18n, fileList.getI18n());
    }

    @Test
    void setI18n_withNull_throws() {
        UploadFileList fileList = new UploadFileList();

        Assertions.assertThrows(NullPointerException.class,
                () -> fileList.setI18n(null));
    }

    @Test
    void i18n_fileClass_allSettersWork() {
        UploadFileListI18N.File file = new UploadFileListI18N.File();

        file.setRetry("Retry").setStart("Start").setRemove("Remove");

        Assertions.assertEquals("Retry", file.getRetry());
        Assertions.assertEquals("Start", file.getStart());
        Assertions.assertEquals("Remove", file.getRemove());
    }

    @Test
    void i18n_errorClass_allSettersWork() {
        UploadFileListI18N.Error error = new UploadFileListI18N.Error();

        error.setTooManyFiles("Too many files")
                .setFileIsTooBig("File is too big")
                .setIncorrectFileType("Incorrect file type");

        Assertions.assertEquals("Too many files", error.getTooManyFiles());
        Assertions.assertEquals("File is too big", error.getFileIsTooBig());
        Assertions.assertEquals("Incorrect file type",
                error.getIncorrectFileType());
    }

    @Test
    void i18n_uploadingStatus_allSettersWork() {
        UploadFileListI18N.Uploading.Status status = new UploadFileListI18N.Uploading.Status();

        status.setConnecting("Connecting").setStalled("Stalled")
                .setProcessing("Processing").setHeld("Held");

        Assertions.assertEquals("Connecting", status.getConnecting());
        Assertions.assertEquals("Stalled", status.getStalled());
        Assertions.assertEquals("Processing", status.getProcessing());
        Assertions.assertEquals("Held", status.getHeld());
    }

    @Test
    void i18n_remainingTime_allSettersWork() {
        UploadFileListI18N.Uploading.RemainingTime remainingTime = new UploadFileListI18N.Uploading.RemainingTime();

        remainingTime.setPrefix("remaining: ")
                .setUnknown("unknown remaining time");

        Assertions.assertEquals("remaining: ", remainingTime.getPrefix());
        Assertions.assertEquals("unknown remaining time",
                remainingTime.getUnknown());
    }

    @Test
    void i18n_uploadError_allSettersWork() {
        UploadFileListI18N.Uploading.UploadError uploadError = new UploadFileListI18N.Uploading.UploadError();

        uploadError.setServerUnavailable("Server unavailable")
                .setUnexpectedServerError("Unexpected server error")
                .setForbidden("Forbidden").setFileTooLarge("File too large");

        Assertions.assertEquals("Server unavailable",
                uploadError.getServerUnavailable());
        Assertions.assertEquals("Unexpected server error",
                uploadError.getUnexpectedServerError());
        Assertions.assertEquals("Forbidden", uploadError.getForbidden());
        Assertions.assertEquals("File too large",
                uploadError.getFileTooLarge());
    }

    @Test
    void i18n_uploading_allSettersWork() {
        UploadFileListI18N.Uploading uploading = new UploadFileListI18N.Uploading();
        UploadFileListI18N.Uploading.Status status = new UploadFileListI18N.Uploading.Status();
        UploadFileListI18N.Uploading.RemainingTime remainingTime = new UploadFileListI18N.Uploading.RemainingTime();
        UploadFileListI18N.Uploading.UploadError error = new UploadFileListI18N.Uploading.UploadError();

        uploading.setStatus(status).setRemainingTime(remainingTime)
                .setError(error);

        Assertions.assertSame(status, uploading.getStatus());
        Assertions.assertSame(remainingTime, uploading.getRemainingTime());
        Assertions.assertSame(error, uploading.getError());
    }

    @Test
    void i18n_units_defaultConstructor() {
        UploadFileListI18N.Units units = new UploadFileListI18N.Units();

        Assertions.assertNotNull(units.getSize());
        Assertions.assertEquals(9, units.getSize().size());
        Assertions.assertTrue(units.getSize().contains("B"));
        Assertions.assertTrue(units.getSize().contains("kB"));
        Assertions.assertTrue(units.getSize().contains("MB"));
    }

    @Test
    void i18n_units_constructorWithSize() {
        var customSizes = Arrays.asList("Bytes", "KB", "MB");
        UploadFileListI18N.Units units = new UploadFileListI18N.Units(
                customSizes);

        Assertions.assertEquals(customSizes, units.getSize());
        Assertions.assertNull(units.getSizeBase());
    }

    @Test
    void i18n_units_constructorWithSizeAndBase() {
        var customSizes = Arrays.asList("Bytes", "KB", "MB");
        UploadFileListI18N.Units units = new UploadFileListI18N.Units(
                customSizes, 1024);

        Assertions.assertEquals(customSizes, units.getSize());
        Assertions.assertEquals(Integer.valueOf(1024), units.getSizeBase());
    }

    @Test
    void i18n_units_setters() {
        UploadFileListI18N.Units units = new UploadFileListI18N.Units();
        var customSizes = Arrays.asList("B", "KB");

        units.setSize(customSizes).setSizeBase(1000);

        Assertions.assertEquals(customSizes, units.getSize());
        Assertions.assertEquals(Integer.valueOf(1000), units.getSizeBase());
    }

    @Test
    void i18n_allSettersWork() {
        UploadFileListI18N i18n = new UploadFileListI18N();
        UploadFileListI18N.File file = new UploadFileListI18N.File();
        UploadFileListI18N.Error error = new UploadFileListI18N.Error();
        UploadFileListI18N.Uploading uploading = new UploadFileListI18N.Uploading();
        UploadFileListI18N.Units units = new UploadFileListI18N.Units();

        i18n.setFile(file).setError(error).setUploading(uploading)
                .setUnits(units);

        Assertions.assertSame(file, i18n.getFile());
        Assertions.assertSame(error, i18n.getError());
        Assertions.assertSame(uploading, i18n.getUploading());
        Assertions.assertSame(units, i18n.getUnits());
    }

    @Test
    void i18n_setUnitsWithList() {
        UploadFileListI18N i18n = new UploadFileListI18N();
        var customSizes = Arrays.asList("B", "KB", "MB");

        i18n.setUnits(customSizes);

        Assertions.assertNotNull(i18n.getUnits());
        Assertions.assertEquals(customSizes, i18n.getUnits().getSize());
    }

    @Test
    void i18n_setUnitsWithListAndBase() {
        UploadFileListI18N i18n = new UploadFileListI18N();
        var customSizes = Arrays.asList("B", "KB", "MB");

        i18n.setUnits(customSizes, 1024);

        Assertions.assertNotNull(i18n.getUnits());
        Assertions.assertEquals(customSizes, i18n.getUnits().getSize());
        Assertions.assertEquals(Integer.valueOf(1024),
                i18n.getUnits().getSizeBase());
    }

    @Test
    void implementsHasSize() {
        Assertions.assertTrue(
                HasSize.class.isAssignableFrom(UploadFileList.class));
    }

    @Test
    void implementsHasEnabled() {
        Assertions.assertTrue(
                HasEnabled.class.isAssignableFrom(UploadFileList.class));
    }

    @Test
    void addThemeVariant_variantIsAdded() {
        UploadFileList fileList = new UploadFileList();

        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        Assertions.assertTrue(fileList.getThemeNames().contains("thumbnails"));
    }

    @Test
    void removeThemeVariant_variantIsRemoved() {
        UploadFileList fileList = new UploadFileList();
        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        fileList.removeThemeVariants(UploadFileListVariant.THUMBNAILS);

        Assertions.assertFalse(fileList.getThemeNames().contains("thumbnails"));
    }
}
