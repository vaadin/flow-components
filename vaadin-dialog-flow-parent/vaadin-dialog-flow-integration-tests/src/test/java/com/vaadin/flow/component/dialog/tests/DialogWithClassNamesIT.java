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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.dialog.tests.DialogTestPageIT.DIALOG_OVERLAY_TAG;

@TestPath("vaadin-dialog/dialog-class-names-test")
public class DialogWithClassNamesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialog_overlayHasSameClassNames() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));
        DialogElement dialog = $(DialogElement.class).first();

        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("custom", dialogClassNames);
        Assert.assertEquals("custom", overlayClassNames);
    }

    @Test
    public void openDialog_overlayChangeClassName() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        $(NativeButtonElement.class).id("add").click();

        DialogElement dialog = $(DialogElement.class).first();
        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("custom added", dialogClassNames);
        Assert.assertEquals("custom added", overlayClassNames);
    }

    @Test
    public void openDialog_overlayNoClassNameAfterClearClassName() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        $(NativeButtonElement.class).id("clear").click();

        DialogElement dialog = $(DialogElement.class).first();
        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("", dialogClassNames);
        Assert.assertEquals("", overlayClassNames);
    }

    @Test
    public void openDialog_overlayChagedClassNameAfterSecondOpening() {
        $(NativeButtonElement.class).id("open").click();

        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        $(NativeButtonElement.class).id("clear").click();
        $(NativeButtonElement.class).id("add").click();

        $(DIALOG_OVERLAY_TAG).first().click();

        $(NativeButtonElement.class).id("open").click();
        waitForElementPresent(By.tagName(DIALOG_OVERLAY_TAG));

        DialogElement dialog = $(DialogElement.class).first();
        WebElement overlay = $(DIALOG_OVERLAY_TAG).first();

        String overlayClassNames = overlay.getAttribute("class");
        String dialogClassNames = dialog.getAttribute("class");

        Assert.assertEquals("added", dialogClassNames);
        Assert.assertEquals("added", overlayClassNames);
    }
}
