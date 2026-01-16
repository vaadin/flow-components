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
package com.vaadin.flow.component.button.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the ButtonView.
 */
@TestPath("vaadin-button/signal-button")
public class SignalButtonIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-button"));
    }

    @Test
    public void textSignalButtons() {
        ButtonElement textSignalButton = $(ButtonElement.class)
                .id("text-signal-button");
        ButtonElement signalIconButton = $(ButtonElement.class)
                .id("signal-icon-button");
        ButtonElement signalIconClickListenerButton = $(ButtonElement.class)
                .id("signal-icon-click-button");
        ButtonElement signalClickButton = $(ButtonElement.class)
                .id("signal-click-button");

        Assert.assertEquals("initial text", textSignalButton.getText());
        Assert.assertEquals("initial text", signalIconButton.getText());
        Assert.assertEquals("initial text",
                signalIconClickListenerButton.getText());
        Assert.assertEquals("initial text", signalClickButton.getText());

        signalIconClickListenerButton.click();

        Assert.assertEquals("signal-icon-click-button clicked",
                textSignalButton.getText());
        Assert.assertEquals("signal-icon-click-button clicked",
                signalIconButton.getText());
        Assert.assertEquals("signal-icon-click-button clicked",
                signalIconClickListenerButton.getText());
        Assert.assertEquals("signal-icon-click-button clicked",
                signalClickButton.getText());

        signalClickButton.click();

        Assert.assertEquals("signal-click-button clicked",
                textSignalButton.getText());
        Assert.assertEquals("signal-click-button clicked",
                signalIconButton.getText());
        Assert.assertEquals("signal-click-button clicked",
                signalIconClickListenerButton.getText());
        Assert.assertEquals("signal-click-button clicked",
                signalClickButton.getText());
    }

    @Test
    public void computedSignalButtons() {
        ButtonElement computedSignalButton = $(ButtonElement.class)
                .id("computed-signal-button");
        Assert.assertEquals("initial text computed",
                computedSignalButton.getText());

        $(ButtonElement.class).id("signal-icon-click-button").click();

        Assert.assertEquals("signal-icon-click-button clicked computed",
                computedSignalButton.getText());

        $(ButtonElement.class).id("signal-click-button").click();

        Assert.assertEquals("signal-click-button clicked computed",
                computedSignalButton.getText());
    }

    @Test
    public void textSignalButton_removingAndAdding() {
        $(ButtonElement.class).id("remove-text-signal-button").click();

        Assert.assertTrue("expected zero #text-signal-button elements",
                findElements(By.id("text-signal-button")).isEmpty());

        $(ButtonElement.class).id("signal-click-button").click();

        $(ButtonElement.class).id("add-text-signal-button").click();

        waitForElementPresent(By.id("text-signal-button"));
        Assert.assertEquals("signal-click-button clicked",
                $(ButtonElement.class).id("text-signal-button").getText());
    }

    @Test
    public void signalIconButton_preservesIcon() {
        ButtonElement iconButton = $(ButtonElement.class)
                .id("signal-icon-button");
        ButtonElement iconClickButton = $(ButtonElement.class)
                .id("signal-icon-click-button");

        TestBenchElement iconButtonIcon = iconButton.$("vaadin-icon").first();
        TestBenchElement iconClickButtonIcon = iconClickButton.$("vaadin-icon")
                .first();
        Assert.assertEquals("prefix", iconButtonIcon.getAttribute("slot"));
        Assert.assertEquals("suffix", iconClickButtonIcon.getAttribute("slot"));

        iconClickButton.click();

        iconButtonIcon = iconButton.$("vaadin-icon").first();
        iconClickButtonIcon = iconClickButton.$("vaadin-icon").first();
        Assert.assertEquals("prefix", iconButtonIcon.getAttribute("slot"));
        Assert.assertEquals("suffix", iconClickButtonIcon.getAttribute("slot"));
    }

    @Test
    public void signalIconButton_iconSlotIsUpdated() {
        ButtonElement iconButton = $(ButtonElement.class)
                .id("signal-icon-button");
        ButtonElement iconClickButton = $(ButtonElement.class)
                .id("signal-icon-click-button");

        TestBenchElement iconButtonIcon = iconButton.$("vaadin-icon").first();
        TestBenchElement iconClickButtonIcon = iconClickButton.$("vaadin-icon")
                .first();

        $(ButtonElement.class).id("clear-text-button").click();

        iconButtonIcon = iconButton.$("vaadin-icon").first();
        iconClickButtonIcon = iconClickButton.$("vaadin-icon").first();
        Assert.assertFalse("should not have slot attribute",
                iconButtonIcon.hasAttribute("slot"));
        Assert.assertFalse("should not have slot attribute",
                iconClickButtonIcon.hasAttribute("slot"));

        iconClickButton.click();

        iconButtonIcon = iconButton.$("vaadin-icon").first();
        iconClickButtonIcon = iconClickButton.$("vaadin-icon").first();
        Assert.assertEquals("prefix", iconButtonIcon.getAttribute("slot"));
        Assert.assertEquals("suffix", iconClickButtonIcon.getAttribute("slot"));
    }

    @Test
    public void signalIconButton_themeAttributeIsUpdated() {
        ButtonElement iconButton = $(ButtonElement.class)
                .id("signal-icon-button");
        ButtonElement iconClickButton = $(ButtonElement.class)
                .id("signal-icon-click-button");
        Assert.assertFalse("should not have theme attribute",
                iconButton.hasAttribute("theme"));
        Assert.assertFalse("should not have theme attribute",
                iconClickButton.hasAttribute("theme"));

        $(ButtonElement.class).id("clear-text-button").click();

        Assert.assertEquals("icon", iconButton.getAttribute("theme"));
        Assert.assertEquals("icon", iconClickButton.getAttribute("theme"));

        iconClickButton.click();

        Assert.assertFalse("should not have theme attribute",
                iconButton.hasAttribute("theme"));
        Assert.assertFalse("should not have theme attribute",
                iconClickButton.hasAttribute("theme"));
    }
}
