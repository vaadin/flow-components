/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.dialog.tests.ModalityDialogsPage.Log.LOG_ID;

@TestPath("vaadin-dialog/dialog-modality")
public class ModalityDialogsPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        Assert.assertEquals("No log messages should exist on open", 0,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

    @Test
    public void openNonModalDialog_logButtonClickable() {
        $(NativeButtonElement.class).id("open-non-modal-dialog").click();

        Assert.assertTrue("No dialog opened", $(DialogElement.class).exists());
        Assert.assertEquals("Only one dialog expected", 1,
                $(DialogElement.class).all().size());

        $(NativeButtonElement.class).id("log").click();

        Assert.assertTrue("Dialog should not have closed",
                $(DialogElement.class).exists());
        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());

        $(DialogElement.class).first().$(NativeButtonElement.class).first()
                .click();

        Assert.assertFalse("Dialog should have closed",
                $(DialogElement.class).exists());
    }

    @Test
    public void openModalDialog_removeBackdrop_logClickNotAccepted() {
        $(NativeButtonElement.class).id("open-modal-dialog").click();
        final DivElement backdrop = $(TestBenchElement.class).id("overlay")
                .$(DivElement.class).id("backdrop");

        executeScript("arguments[0].remove()", backdrop);

        Assert.assertFalse("Backdrop was not removed from dom",
                $(TestBenchElement.class).id("overlay").$(DivElement.class)
                        .attributeContains("id", "backdrop").exists());

        $(NativeButtonElement.class).id("log").click();

        Assert.assertTrue("Dialog should not have closed",
                $(DialogElement.class).exists());
        Assert.assertEquals("Click on button should not generate a log message",
                0, $(DivElement.class).id(LOG_ID).$("div").all().size());

        $(DialogElement.class).first().$(NativeButtonElement.class).id("close")
                .click();

        Assert.assertFalse("Dialog should have closed",
                $(DialogElement.class).exists());

        $(NativeButtonElement.class).id("log").click();

        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

    @Test
    public void openModalDialog_outsideClickToCloseDialog_logButtonClickable() {
        $(NativeButtonElement.class).id("add-modal-dialog").click();
        $(NativeButtonElement.class).id("enable-close-on-outside-click")
                .click();
        $(NativeButtonElement.class).id("open-modal-dialog").click();

        // Click anything to close dialog
        $("body").first().click();
        Assert.assertFalse("Dialog should be hidden",
                $(DialogElement.class).first().isDisplayed());

        // Now that dialog is closed, verify that click events work
        $(NativeButtonElement.class).id("log").click();
        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

    @Test
    public void openModalDialog_openNonModalOnTop_nonModalCanBeUsed() {
        $(NativeButtonElement.class).id("open-modal-dialog").click();

        final DialogElement first = $(DialogElement.class).first();
        first.$(NativeButtonElement.class).id("open-sub").click();

        waitUntil(driver -> $(DialogElement.class).all().size() == 2);

        final DialogElement subDialog = $(DialogElement.class).all().stream()
                .filter(dialog -> !dialog.equals(first)).findFirst().get();
        subDialog.$(NativeButtonElement.class).id("log-sub").click();

        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());

        subDialog.$(NativeButtonElement.class).id("close-sub").click();

        first.$(NativeButtonElement.class).id("close").click();
    }

    @Test
    public void openModalDialog_hideComponent_logClickAccepted() {
        $(NativeButtonElement.class).id("open-modal-dialog").click();

        $(DialogElement.class).first().$(NativeButtonElement.class).id("hide")
                .click();

        Assert.assertTrue("Dialog should not have closed",
                $(DialogElement.class).exists());
        Assert.assertFalse("Dialog should be hidden",
                $(DialogElement.class).first().isDisplayed());

        $(NativeButtonElement.class).id("log").click();

        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());

        $(NativeButtonElement.class).id("show").click();

        $(DialogElement.class).first().$(NativeButtonElement.class).id("close")
                .click();

        Assert.assertFalse("Dialog should have closed",
                $(DialogElement.class).exists());
    }
}
