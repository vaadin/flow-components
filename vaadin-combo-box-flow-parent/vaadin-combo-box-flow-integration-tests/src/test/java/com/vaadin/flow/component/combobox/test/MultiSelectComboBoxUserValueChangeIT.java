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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Verifies that the TestBench element helpers, which change the selection
 * through the {@code change} event, propagate the value to the server.
 */
@TestPath("vaadin-multi-select-combo-box/user-value-change")
public class MultiSelectComboBoxUserValueChangeIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement eventValue;
    private TestBenchElement eventOrigin;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        eventValue = $("span").id("event-value");
        eventOrigin = $("span").id("event-origin");
    }

    @Test
    public void selectItem_valuePropagatedToServer() {
        comboBox.selectByText("Item 1");

        waitUntil(driver -> "Item 1".equals(eventValue.getText()));
        Assert.assertEquals("client", eventOrigin.getText());
    }

    @Test
    public void deselectItem_valuePropagatedToServer() {
        comboBox.selectByText("Item 1");
        waitUntil(driver -> "Item 1".equals(eventValue.getText()));

        comboBox.deselectByText("Item 1");
        waitUntil(driver -> eventValue.getText().isEmpty());
        Assert.assertEquals("client", eventOrigin.getText());
    }

    @Test
    public void deselectAll_valuePropagatedToServer() {
        comboBox.selectByText("Item 1");
        waitUntil(driver -> "Item 1".equals(eventValue.getText()));

        comboBox.deselectAll();
        waitUntil(driver -> eventValue.getText().isEmpty());
        Assert.assertEquals("client", eventOrigin.getText());
    }
}
