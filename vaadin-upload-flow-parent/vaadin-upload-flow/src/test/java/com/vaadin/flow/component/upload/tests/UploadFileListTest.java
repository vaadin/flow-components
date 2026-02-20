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
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.upload.ModularUploadFeatureFlagProvider;
import com.vaadin.flow.component.upload.UploadFileList;
import com.vaadin.flow.component.upload.UploadFileListI18N;
import com.vaadin.flow.component.upload.UploadFileListVariant;
import com.vaadin.flow.component.upload.UploadManager;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.tests.EnableFeatureFlagRule;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class UploadFileListTest {
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            ModularUploadFeatureFlagProvider.MODULAR_UPLOAD);

    private UI ui;
    private Div owner;
    private UploadManager manager;

    @Before
    public void setup() {
        ui = Mockito.spy(new UI());
        UI.setCurrent(ui);

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        VaadinService mockService = Mockito.mock(VaadinService.class);
        StreamResourceRegistry streamResourceRegistry = new StreamResourceRegistry(
                mockSession);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistry);
        Mockito.when(mockSession.access(Mockito.any()))
                .thenAnswer(invocation -> {
                    invocation.getArgument(0, Command.class).execute();
                    return new CompletableFuture<>();
                });
        Mockito.when(mockSession.getService()).thenReturn(mockService);
        ui.getInternals().setSession(mockSession);

        owner = new Div();
        ui.add(owner);
        manager = new UploadManager(owner);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void constructor_default_createsFileList() {
        UploadFileList fileList = new UploadFileList();

        Assert.assertNotNull(fileList);
        Assert.assertEquals("vaadin-upload-file-list",
                fileList.getElement().getTag());
    }

    @Test
    public void constructor_withManager_linksToManager() {
        UploadFileList fileList = new UploadFileList(manager);

        Assert.assertSame(manager, fileList.getUploadManager());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNull_throws() {
        new UploadFileList(null);
    }

    @Test
    public void setUploadManager_linksToManager() {
        UploadFileList fileList = new UploadFileList();

        fileList.setUploadManager(manager);

        Assert.assertSame(manager, fileList.getUploadManager());
    }

    @Test
    public void getUploadManager_default_returnsNull() {
        UploadFileList fileList = new UploadFileList();

        Assert.assertNull(fileList.getUploadManager());
    }

    @Test
    public void getI18n_initiallyNull() {
        UploadFileList fileList = new UploadFileList();

        Assert.assertNull(fileList.getI18n());
    }

    @Test
    public void setI18n_setsI18n() {
        UploadFileList fileList = new UploadFileList();
        UploadFileListI18N i18n = new UploadFileListI18N();
        i18n.setFile(new UploadFileListI18N.File().setRetry("Retry"));

        fileList.setI18n(i18n);

        Assert.assertSame(i18n, fileList.getI18n());
    }

    @Test(expected = NullPointerException.class)
    public void setI18n_withNull_throws() {
        UploadFileList fileList = new UploadFileList();

        fileList.setI18n(null);
    }

    @Test
    public void i18n_fileClass_allSettersWork() {
        UploadFileListI18N.File file = new UploadFileListI18N.File();

        file.setRetry("Retry").setStart("Start").setRemove("Remove");

        Assert.assertEquals("Retry", file.getRetry());
        Assert.assertEquals("Start", file.getStart());
        Assert.assertEquals("Remove", file.getRemove());
    }

    @Test
    public void i18n_errorClass_allSettersWork() {
        UploadFileListI18N.Error error = new UploadFileListI18N.Error();

        error.setTooManyFiles("Too many files")
                .setFileIsTooBig("File is too big")
                .setIncorrectFileType("Incorrect file type");

        Assert.assertEquals("Too many files", error.getTooManyFiles());
        Assert.assertEquals("File is too big", error.getFileIsTooBig());
        Assert.assertEquals("Incorrect file type",
                error.getIncorrectFileType());
    }

    @Test
    public void i18n_uploadingStatus_allSettersWork() {
        UploadFileListI18N.Uploading.Status status = new UploadFileListI18N.Uploading.Status();

        status.setConnecting("Connecting").setStalled("Stalled")
                .setProcessing("Processing").setHeld("Held");

        Assert.assertEquals("Connecting", status.getConnecting());
        Assert.assertEquals("Stalled", status.getStalled());
        Assert.assertEquals("Processing", status.getProcessing());
        Assert.assertEquals("Held", status.getHeld());
    }

    @Test
    public void i18n_remainingTime_allSettersWork() {
        UploadFileListI18N.Uploading.RemainingTime remainingTime = new UploadFileListI18N.Uploading.RemainingTime();

        remainingTime.setPrefix("remaining: ")
                .setUnknown("unknown remaining time");

        Assert.assertEquals("remaining: ", remainingTime.getPrefix());
        Assert.assertEquals("unknown remaining time",
                remainingTime.getUnknown());
    }

    @Test
    public void i18n_uploadError_allSettersWork() {
        UploadFileListI18N.Uploading.UploadError uploadError = new UploadFileListI18N.Uploading.UploadError();

        uploadError.setServerUnavailable("Server unavailable")
                .setUnexpectedServerError("Unexpected server error")
                .setForbidden("Forbidden").setFileTooLarge("File too large");

        Assert.assertEquals("Server unavailable",
                uploadError.getServerUnavailable());
        Assert.assertEquals("Unexpected server error",
                uploadError.getUnexpectedServerError());
        Assert.assertEquals("Forbidden", uploadError.getForbidden());
        Assert.assertEquals("File too large", uploadError.getFileTooLarge());
    }

    @Test
    public void i18n_uploading_allSettersWork() {
        UploadFileListI18N.Uploading uploading = new UploadFileListI18N.Uploading();
        UploadFileListI18N.Uploading.Status status = new UploadFileListI18N.Uploading.Status();
        UploadFileListI18N.Uploading.RemainingTime remainingTime = new UploadFileListI18N.Uploading.RemainingTime();
        UploadFileListI18N.Uploading.UploadError error = new UploadFileListI18N.Uploading.UploadError();

        uploading.setStatus(status).setRemainingTime(remainingTime)
                .setError(error);

        Assert.assertSame(status, uploading.getStatus());
        Assert.assertSame(remainingTime, uploading.getRemainingTime());
        Assert.assertSame(error, uploading.getError());
    }

    @Test
    public void i18n_units_defaultConstructor() {
        UploadFileListI18N.Units units = new UploadFileListI18N.Units();

        Assert.assertNotNull(units.getSize());
        Assert.assertEquals(9, units.getSize().size());
        Assert.assertTrue(units.getSize().contains("B"));
        Assert.assertTrue(units.getSize().contains("kB"));
        Assert.assertTrue(units.getSize().contains("MB"));
    }

    @Test
    public void i18n_units_constructorWithSize() {
        var customSizes = Arrays.asList("Bytes", "KB", "MB");
        UploadFileListI18N.Units units = new UploadFileListI18N.Units(
                customSizes);

        Assert.assertEquals(customSizes, units.getSize());
        Assert.assertNull(units.getSizeBase());
    }

    @Test
    public void i18n_units_constructorWithSizeAndBase() {
        var customSizes = Arrays.asList("Bytes", "KB", "MB");
        UploadFileListI18N.Units units = new UploadFileListI18N.Units(
                customSizes, 1024);

        Assert.assertEquals(customSizes, units.getSize());
        Assert.assertEquals(Integer.valueOf(1024), units.getSizeBase());
    }

    @Test
    public void i18n_units_setters() {
        UploadFileListI18N.Units units = new UploadFileListI18N.Units();
        var customSizes = Arrays.asList("B", "KB");

        units.setSize(customSizes).setSizeBase(1000);

        Assert.assertEquals(customSizes, units.getSize());
        Assert.assertEquals(Integer.valueOf(1000), units.getSizeBase());
    }

    @Test
    public void i18n_allSettersWork() {
        UploadFileListI18N i18n = new UploadFileListI18N();
        UploadFileListI18N.File file = new UploadFileListI18N.File();
        UploadFileListI18N.Error error = new UploadFileListI18N.Error();
        UploadFileListI18N.Uploading uploading = new UploadFileListI18N.Uploading();
        UploadFileListI18N.Units units = new UploadFileListI18N.Units();

        i18n.setFile(file).setError(error).setUploading(uploading)
                .setUnits(units);

        Assert.assertSame(file, i18n.getFile());
        Assert.assertSame(error, i18n.getError());
        Assert.assertSame(uploading, i18n.getUploading());
        Assert.assertSame(units, i18n.getUnits());
    }

    @Test
    public void i18n_setUnitsWithList() {
        UploadFileListI18N i18n = new UploadFileListI18N();
        var customSizes = Arrays.asList("B", "KB", "MB");

        i18n.setUnits(customSizes);

        Assert.assertNotNull(i18n.getUnits());
        Assert.assertEquals(customSizes, i18n.getUnits().getSize());
    }

    @Test
    public void i18n_setUnitsWithListAndBase() {
        UploadFileListI18N i18n = new UploadFileListI18N();
        var customSizes = Arrays.asList("B", "KB", "MB");

        i18n.setUnits(customSizes, 1024);

        Assert.assertNotNull(i18n.getUnits());
        Assert.assertEquals(customSizes, i18n.getUnits().getSize());
        Assert.assertEquals(Integer.valueOf(1024),
                i18n.getUnits().getSizeBase());
    }

    @Test
    public void implementsHasSize() {
        Assert.assertTrue(HasSize.class.isAssignableFrom(UploadFileList.class));
    }

    @Test
    public void implementsHasEnabled() {
        Assert.assertTrue(
                HasEnabled.class.isAssignableFrom(UploadFileList.class));
    }

    @Test
    public void addThemeVariant_variantIsAdded() {
        UploadFileList fileList = new UploadFileList();

        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        Assert.assertTrue(fileList.getThemeNames().contains("thumbnails"));
    }

    @Test
    public void removeThemeVariant_variantIsRemoved() {
        UploadFileList fileList = new UploadFileList();
        fileList.addThemeVariants(UploadFileListVariant.THUMBNAILS);

        fileList.removeThemeVariants(UploadFileListVariant.THUMBNAILS);

        Assert.assertFalse(fileList.getThemeNames().contains("thumbnails"));
    }
}
