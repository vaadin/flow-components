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

@TestPath("vaadin-button/accessible-disabled-button")
public class AccessibleDisabledButtonIT extends AbstractComponentIT {
    private ButtonElement button;
    private TestBenchElement listenerCounters;

    @Before
    public void init() {
        open();
        button = $(ButtonElement.class).first();
        listenerCounters = $("div").id("listener-counters");
    }

    @Test
    public void click_noClickEvent() {
        button.click();
        assertListenerCounter("click", 0);
    }

    @Test
    public void doubleClick_noDoubleClickEvent() {
        button.doubleClick();
        assertListenerCounter("doubleClick", 0);
    }

    @Test
    public void focus_focusEventIsFired() {
        button.focus();
        assertListenerCounter("focus", 1);
    }

    @Test
    public void focusWithShortcut_focusEventIsFired() {
        new Actions(getDriver()).keyDown(Keys.ALT).sendKeys("A").keyUp(Keys.ALT)
                .build().perform();
        assertListenerCounter("focus", 1);
    }

    @Test
    public void blur_blurEventIsFired() {
        button.sendKeys(Keys.TAB);
        assertListenerCounter("blur", 1);
    }

    private void assertListenerCounter(String name, int expectedCount) {
        JsonObject json = Json.parse(listenerCounters.getText());
        Assert.assertEquals("Unexpected " + name + " value", expectedCount,
                (int) json.getNumber(name));
    }
}
