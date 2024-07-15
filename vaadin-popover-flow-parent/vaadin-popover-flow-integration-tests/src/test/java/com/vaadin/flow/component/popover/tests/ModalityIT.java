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
 *
 */
package com.vaadin.flow.component.popover.tests;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;

@TestPath("vaadin-popover/modality")
public class ModalityIT extends AbstractComponentIT {

    static final String POPOVER_OVERLAY_TAG = "vaadin-popover-overlay";

    private NativeButtonElement target;
    private NativeButtonElement setModal;
    private NativeButtonElement setNonModal;
    private NativeButtonElement testClick;
    private SpanElement testClickResult;

    @Before
    public void init() {
        open();
        target = $(NativeButtonElement.class).id("popover-target");
        setModal = $(NativeButtonElement.class).id("set-modal");
        setNonModal = $(NativeButtonElement.class).id("set-non-modal");
        testClick = $(NativeButtonElement.class).id("test-click");
        testClickResult = $(SpanElement.class).id("test-click-result");
    }

    @Test
    public void openPopover_clicksOnElementsInBackgroundAllowed() {
        target.click();
        checkPopoverIsOpened();
        assertBackgroundButtonIsClickable();
    }

    @Test
    public void setModal_openPopover_noClicksOnElementsInBackgroundAllowed() {
        setModal.click();
        target.click();
        checkPopoverIsOpened();
        assertBackgroundButtonIsNotClickable();
    }

    @Test
    public void setModal_openAndClosePopover_clicksOnElementsInBackgroundAllowed() {
        setModal.click();
        target.click();
        checkPopoverIsOpened();

        target.click();
        checkPopoverIsClosed();
        assertBackgroundButtonIsClickable();
    }

    @Test
    public void setNonModal_openPopover_clicksOnElementsInBackgroundAllowed() {
        setModal.click();
        setNonModal.click();
        target.click();
        checkPopoverIsOpened();
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

    private void checkPopoverIsClosed() {
        waitForElementNotPresent(By.tagName(POPOVER_OVERLAY_TAG));
    }

    private void checkPopoverIsOpened() {
        waitForElementPresent(By.tagName(POPOVER_OVERLAY_TAG));
    }
}
