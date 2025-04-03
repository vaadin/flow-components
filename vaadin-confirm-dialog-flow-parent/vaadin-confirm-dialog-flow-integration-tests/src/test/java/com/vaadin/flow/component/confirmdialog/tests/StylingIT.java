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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-confirm-dialog/styling")
public class StylingIT extends AbstractComponentIT {

    private ButtonElement addDialog;
    private ButtonElement openDialog;
    private ButtonElement addFoo;

    @Before
    public void init() {
        open();
        addDialog = $(ButtonElement.class).id("add-dialog");
        openDialog = $(ButtonElement.class).id("open-dialog");
        addFoo = $(ButtonElement.class).id("add-foo");
    }

    @Test
    public void addClassBeforeAdd() {
        addFoo.click();

        addDialog.click();
        openDialog.click();

        String value = getOverlayClassName();
        Assert.assertEquals("foo", value);
    }

    @Test
    public void addClassBeforeOpen() {
        addFoo.click();

        openDialog.click();

        String value = getOverlayClassName();
        Assert.assertEquals("foo", value);
    }

    @Test
    public void addClassAfterOpen() {
        openDialog.click();

        $(ButtonElement.class).id("set-bar").click();

        String value = getOverlayClassName();
        Assert.assertEquals("bar", value);
    }

    @Test
    public void removeClassAfterOpen() {
        addFoo.click();
        openDialog.click();

        $(ButtonElement.class).id("remove-all").click();

        String value = getOverlayClassName();
        Assert.assertEquals("", value);
    }

    private ConfirmDialogElement getConfirmDialog() {
        return $(ConfirmDialogElement.class).waitForFirst();
    }

    private TestBenchElement getOverlay() {
        return ((TestBenchElement) getConfirmDialog().getContext());
    }

    private String getOverlayClassName() {
        return getOverlay().getDomAttribute("class");
    }
}
