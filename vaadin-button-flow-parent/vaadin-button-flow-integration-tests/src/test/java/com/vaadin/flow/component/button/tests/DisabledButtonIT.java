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
package com.vaadin.flow.component.button.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import elemental.json.Json;
import elemental.json.JsonObject;

@TestPath("vaadin-button/disabled-button")
public class DisabledButtonIT extends AbstractComponentIT {
    private ButtonElement button;
    private TestBenchElement listenerCounters;

    @Before
    public void init() {
        open();
        button = $(ButtonElement.class).first();
        listenerCounters = $("div").id("listener-counters");
    }

    @Test
    public void click_noClickEventWhenDisabled() {
        button.click();
        assertListenerCounter("click", 0);

        $("button").id("enable-button").click();

        button.click();
        assertListenerCounter("click", 1);
    }

    @Test
    public void doubleClick_noDoubleClickEventWhenDisabled() {
        button.doubleClick();
        assertListenerCounter("doubleClick", 0);

        $("button").id("enable-button").click();

        button.doubleClick();
        assertListenerCounter("doubleClick", 1);
    }

    @Test
    public void focus_firesFocusEventWhenDisabled() {
        button.focus();
        assertListenerCounter("focus", 1);
    }

    @Test
    public void focusWithShortcut_firesFocusEventWhenDisabled() {
        new Actions(getDriver()).keyDown(Keys.ALT).sendKeys("A").keyUp(Keys.ALT)
                .build().perform();
        assertListenerCounter("focus", 1);
    }

    @Test
    public void blur_firesBlurEventWhenDisabled() {
        button.sendKeys(Keys.TAB);
        assertListenerCounter("blur", 1);
    }

    private void assertListenerCounter(String name, int expectedCount) {
        int actualCount = 0;

        if (!listenerCounters.getText().isEmpty()) {
            JsonObject json = Json.parse(listenerCounters.getText());
            actualCount = (int) json.getNumber(name);
        }

        Assert.assertEquals("Unexpected " + name + " value", expectedCount,
                actualCount);
    }
}
