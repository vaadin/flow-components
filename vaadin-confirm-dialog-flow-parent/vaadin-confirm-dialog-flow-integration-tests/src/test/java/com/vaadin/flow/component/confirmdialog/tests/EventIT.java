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
package com.vaadin.flow.component.confirmdialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/events")
public class EventIT extends AbstractComponentIT {

    private ButtonElement openDialogBtn;
    private ConfirmDialogElement dialog;

    @Before
    public void init() {
        open();
        openDialogBtn = $(ButtonElement.class).id("open-dialog");
        dialog = $(ConfirmDialogElement.class).first();
    }

    @Test
    public void openDialog_closeOnEsc() {
        openDialogBtn.click();
        Assert.assertTrue("Dialog must be opened",
                dialog.getPropertyBoolean("opened"));

        closeDialogByEscKey();

        Assert.assertFalse("Dialog must be closed after esc key",
                dialog.getPropertyBoolean("opened"));
    }

    @Test
    public void openDialog_closeOnEscIsDisallowed() {
        openDialogBtn.click();
        Assert.assertTrue("Dialog must be opened",
                dialog.getPropertyBoolean("opened"));

        toggleCloseOnEsc();

        closeDialogByEscKey();

        Assert.assertTrue("Dialog must be open after esc key",
                dialog.getPropertyBoolean("opened"));
    }

    @Test
    public void testCloseOnEscDialog_closeOnEscIsRestored() {
        openDialogBtn.click();
        Assert.assertTrue("Dialog must be opened",
                dialog.getPropertyBoolean("opened"));

        toggleCloseOnEsc();
        toggleCloseOnEsc();

        closeDialogByEscKey();

        Assert.assertFalse("Dialog must be closed after esc key",
                dialog.getPropertyBoolean("opened"));
    }

    private void toggleCloseOnEsc() {
        $(ButtonElement.class).id("toggle-close-on-esc").click();
    }

    private void closeDialogByEscKey() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }
}
