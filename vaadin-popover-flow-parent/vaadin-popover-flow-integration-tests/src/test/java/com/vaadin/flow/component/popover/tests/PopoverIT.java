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
package com.vaadin.flow.component.popover.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.popover.testbench.PopoverElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link PopoverView}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-popover")
public class PopoverIT extends AbstractComponentIT {

    PopoverElement popover;

    @Before
    public void init() {
        open();
        popover = $(PopoverElement.class).first();
    }

    @Test
    public void clickTarget_popoverOpensAndCloses() {
        clickTarget();
        Assert.assertTrue(popover.isOpen());

        clickTarget();
        Assert.assertFalse(popover.isOpen());
    }

    @Test
    public void clickButton_popoverContainsContent() {
        clickTarget();

        waitForElementPresent(By.id("popover-content"));
    }

    @Test
    public void detachAndReattachTarget_clickTarget_popoverOpensAndCloses() {
        clickElementWithJs("detach-target");
        clickElementWithJs("attach-target");

        clickTarget();
        Assert.assertTrue(popover.isOpen());

        clickTarget();
        Assert.assertFalse(popover.isOpen());
    }

    @Test
    public void clearTarget_clickTarget_popoverDoesNotOpen() {
        clickElementWithJs("clear-target");

        clickTarget();
        Assert.assertFalse(popover.isOpen());
    }

    @Test
    public void detachTarget_clearAndReattach_clickTarget_popoverDoesNotOpen() {
        clickElementWithJs("detach-target");
        clickElementWithJs("clear-target");
        clickElementWithJs("attach-target");

        clickTarget();
        Assert.assertFalse(popover.isOpen());
    }

    @Test
    public void clickOutside_popoverCloses() {
        clickTarget();
        Assert.assertTrue(popover.isOpen());

        $("body").first().click();
        Assert.assertFalse(popover.isOpen());
    }

    @Test
    public void disableCloseOnOutsideClick_clickOutside_popoverDoesNotClose() {
        clickElementWithJs("disable-close-on-outside-click");

        clickTarget();
        Assert.assertTrue(popover.isOpen());

        $("body").first().click();
        Assert.assertTrue(popover.isOpen());
    }

    @Test
    @Ignore("The test passes locally but fails on CI")
    public void pressEsc_popoverCloses() {
        clickTarget();
        Assert.assertTrue(popover.isOpen());

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
        Assert.assertFalse(popover.isOpen());
    }

    @Test
    public void disableCloseOnEsc_pressEsc_popoverDoesNotClose() {
        clickElementWithJs("disable-close-on-esc");

        clickTarget();
        Assert.assertTrue(popover.isOpen());

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
        Assert.assertTrue(popover.isOpen());
    }

    private void clickTarget() {
        clickElementWithJs("popover-target");
    }
}
