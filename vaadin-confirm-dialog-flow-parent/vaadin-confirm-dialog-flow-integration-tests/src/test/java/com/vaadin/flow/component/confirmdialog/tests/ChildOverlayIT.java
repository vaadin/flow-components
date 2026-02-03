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
package com.vaadin.flow.component.confirmdialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/child-overlay")
public class ChildOverlayIT extends AbstractComponentIT {

    private ButtonElement openDialogs;

    @Before
    public void init() {
        open();

        openDialogs = $(ButtonElement.class).id("open-dialogs");
    }

    @Test
    public void openDialogs_parentDialogMessageIsVisible() {
        openDialogs.click();
        var parentDialogElement = $(ConfirmDialogElement.class)
                .id("parent-dialog");
        var messageElement = (TestBenchElement) executeScript(
                "return arguments[0].querySelector(':scope > div')",
                parentDialogElement);

        Assert.assertEquals("This is the parent dialog",
                messageElement.getText());
        Assert.assertTrue(messageElement.isDisplayed());
    }

}
