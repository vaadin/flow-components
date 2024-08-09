/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/modality")
public class ModalityIT extends AbstractComponentIT {

    private ButtonElement addDialog;
    private ButtonElement openDialog;
    private ButtonElement testClick;
    private SpanElement testClickResult;

    @Before
    public void init() {
        open();
        addDialog = $(ButtonElement.class).id("add-dialog");
        openDialog = $(ButtonElement.class).id("open-dialog");
        testClick = $(ButtonElement.class).id("test-click");
        testClickResult = $(SpanElement.class).id("test-click-result");
    }

    @Test
    public void openDialog_noClicksOnElementsInBackgroundAllowed() {
        openDialog.click();
        $(ConfirmDialogElement.class).waitForFirst();
        assertBackgroundButtonIsNotClickable();
    }

    @Test
    public void openAndCloseDialog_clicksOnElementsInBackgroundAllowed() {
        openDialog.click();
        ConfirmDialogElement dialogElement = $(ConfirmDialogElement.class)
                .waitForFirst();
        dialogElement.getConfirmButton().click();
        assertBackgroundButtonIsClickable();
    }

    @Test
    public void addDialog_openDialog_noClicksOnElementsInBackgroundAllowed() {
        addDialog.click();
        openDialog.click();
        $(ConfirmDialogElement.class).waitForFirst();
        assertBackgroundButtonIsNotClickable();
    }

    @Test
    public void addDialog_openAndCloseDialog_clicksOnElementsInBackgroundAllowed() {
        addDialog.click();
        openDialog.click();
        ConfirmDialogElement dialogElement = $(ConfirmDialogElement.class)
                .waitForFirst();
        dialogElement.getConfirmButton().click();
        assertBackgroundButtonIsClickable();
    }

    private void assertBackgroundButtonIsNotClickable() {
        testClick.click();
        Assert.assertEquals("Button in background is clickable", "",
                testClickResult.getText());
    }

    private void assertBackgroundButtonIsClickable() {
        testClick.click();
        Assert.assertEquals("Button in background is not clickable",
                "Click event received", testClickResult.getText());
    }
}
