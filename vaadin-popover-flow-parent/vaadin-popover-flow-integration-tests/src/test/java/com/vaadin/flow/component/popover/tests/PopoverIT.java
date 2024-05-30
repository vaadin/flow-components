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
import com.vaadin.flow.component.popover.testbench.PopoverElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link PopoverView}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-popover")
public class PopoverIT extends AbstractComponentIT {

    static final String POPOVER_OVERLAY_TAG = "vaadin-popover-overlay";

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-popover"))
                .size() > 0);
    }

    @Test
    public void clickTarget_popoverOpensAndCloses() {
        clickTarget();
        checkPopoverIsOpened();

        clickTarget();
        checkPopoverIsClosed();
    }

    @Test
    public void clickButton_popoverContainsContent() {
        clickTarget();
        checkPopoverIsOpened();

        waitForElementPresent(By.id("popover-content"));

        PopoverElement popover = $(PopoverElement.class).first();
        Assert.assertTrue("Popover content is rendered",
                popover.$("div").attribute("id", "popover-content").exists());
    }

    @Test
    public void detachAndReattachTarget_clickTarget_popoverOpensAndCloses() {
        $(NativeButtonElement.class).id("detach-target").click();
        $(NativeButtonElement.class).id("attach-target").click();

        clickTarget();
        checkPopoverIsOpened();

        clickTarget();
        checkPopoverIsClosed();
    }

    @Test
    public void clearTarget_clickTarget_popoverDoesNotOpen() {
        $(NativeButtonElement.class).id("clear-target").click();

        clickTarget();
        checkPopoverIsClosed();
    }

    private void clickTarget() {
        clickElementWithJs("popover-target");
    }

    private void checkPopoverIsClosed() {
        waitForElementNotPresent(By.tagName(POPOVER_OVERLAY_TAG));
    }

    private void checkPopoverIsOpened() {
        waitForElementPresent(By.tagName(POPOVER_OVERLAY_TAG));

        // Wait for the overlay rendering to complete
        getCommandExecutor().getDriver()
                .executeAsyncScript("requestAnimationFrame(arguments[0])");
    }
}
