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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.upload.testbench.UploadElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-upload/i18n")
public class UploadI18nIT extends AbstractUploadIT {
    @Test
    public void setFullI18n_updatesTranslations() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-full-i18n");
        WebElement addButton = upload.$("*").withAttribute("slot", "add-button")
                .first();
        WebElement dropLabel = upload.$("*").withAttribute("slot", "drop-label")
                .first();

        Assert.assertEquals(UploadTestsI18N.RUSSIAN_FULL.getAddFiles().getOne(),
                addButton.getText());
        Assert.assertEquals(
                UploadTestsI18N.RUSSIAN_FULL.getDropFiles().getOne(),
                dropLabel.getText());
    }

    @Test
    public void setPartialI18n_mergesTranslationsWithDefaults() {
        open();

        UploadElement upload = $(UploadElement.class).id("upload-partial-i18n");
        WebElement addButton = upload.$("*").withAttribute("slot", "add-button")
                .first();
        WebElement dropLabel = upload.$("*").withAttribute("slot", "drop-label")
                .first();

        // This label should still be the default one
        Assert.assertEquals("Upload File...", addButton.getText());
        // This one should be overwritten by the UploadI18N config
        Assert.assertEquals(
                UploadTestsI18N.RUSSIAN_PARTIAL.getDropFiles().getOne(),
                dropLabel.getText());
    }

    @Test
    public void setI18n_detach_reattach_i18nPreserved() {
        open();

        WebElement btnSetI18n = findElement(By.id("btn-set-i18n"));
        WebElement btnToggleAttached = findElement(
                By.id("btn-toggle-attached"));

        btnSetI18n.click();

        btnToggleAttached.click();
        btnToggleAttached.click();

        UploadElement upload = $(UploadElement.class)
                .id("upload-detach-reattach-i18n");

        WebElement dropLabel = upload.$("*").withAttribute("slot", "drop-label")
                .first();

        Assert.assertEquals(
                UploadTestsI18N.RUSSIAN_FULL.getDropFiles().getOne(),
                dropLabel.getText());
    }
}
