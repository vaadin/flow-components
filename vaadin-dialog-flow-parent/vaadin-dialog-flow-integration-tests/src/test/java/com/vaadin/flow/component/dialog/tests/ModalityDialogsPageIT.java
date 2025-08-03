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
package com.vaadin.flow.component.dialog.tests;

import static com.vaadin.flow.component.dialog.tests.ModalityDialogsPage.Log.LOG_ID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog/dialog-modality")
public class ModalityDialogsPageIT extends AbstractDialogIT {

    @Before
    public void init() {
        open();
        Assert.assertEquals("No log messages should exist on open", 0,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

    @Test
    public void openNonModalDialog_logButtonClickable() {
        $(NativeButtonElement.class).id("open-non-modal-dialog").click();
        verifyNumberOfDialogs(1);

        $(NativeButtonElement.class).id("log").click();

        verifyOpened();
        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());

        getDialog().$(NativeButtonElement.class).first().click();

        verifyClosedAndRemoved();
    }

    @Test
    public void openModalDialog_removeBackdrop_logClickNotAccepted() {
        $(NativeButtonElement.class).id("open-modal-dialog").click();
        final DivElement backdrop = getDialog().$("*").id("overlay")
                .$(DivElement.class).id("backdrop");

        executeScript("arguments[0].remove()", backdrop);

        Assert.assertFalse("Backdrop was not removed from dom",
                getOverlayComponent(getDialog()).$(DivElement.class)
                        .withAttribute("id", "backdrop").exists());

        $(NativeButtonElement.class).id("log").click();

        verifyOpened();
        Assert.assertEquals("Click on button should not generate a log message",
                0, $(DivElement.class).id(LOG_ID).$("div").all().size());

        getDialog().$(NativeButtonElement.class).id("close").click();

        verifyClosedAndRemoved();

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
        verifyClosed();

        // Now that dialog is closed, verify that click events work
        $(NativeButtonElement.class).id("log").click();
        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());
    }

    @Test
    public void openModalDialog_openNonModalOnTop_nonModalCanBeUsed() {
        $(NativeButtonElement.class).id("open-modal-dialog").click();

        final DialogElement dialog = getDialog();
        dialog.$(NativeButtonElement.class).id("open-sub").click();

        verifyNumberOfDialogs(2);

        final DialogElement subDialog = getDialogs().get(1);
        subDialog.$(NativeButtonElement.class).id("log-sub").click();

        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());

        subDialog.$(NativeButtonElement.class).id("close-sub").click();

        dialog.$(NativeButtonElement.class).id("close").click();

        verifyClosedAndRemoved();
    }

    @Test
    public void openModalDialog_hideComponent_logClickAccepted() {
        $(NativeButtonElement.class).id("open-modal-dialog").click();

        getDialog().$(NativeButtonElement.class).id("hide").click();

        verifyOpened();
        Assert.assertFalse("Dialog should be hidden",
                getDialog().isDisplayed());

        $(NativeButtonElement.class).id("log").click();

        Assert.assertEquals("Click should have resulted in a log message", 1,
                $(DivElement.class).id(LOG_ID).$("div").all().size());

        $(NativeButtonElement.class).id("show").click();

        getDialog().$(NativeButtonElement.class).id("close").click();

        verifyClosedAndRemoved();
    }
}
