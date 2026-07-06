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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Verifies that genuine user value commits still propagate to the server after
 * the value sync was switched from the {@code selected-items-changed} property
 * notification to the {@code change} event (see
 * <a href="https://github.com/vaadin/flow-components/issues/9611">#9611</a>).
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
        clickItem("Item 1");

        waitUntil(driver -> "Item 1".equals(eventValue.getText()));
        Assert.assertEquals("client", eventOrigin.getText());
    }

    @Test
    public void deselectItem_valuePropagatedToServer() {
        clickItem("Item 1");
        waitUntil(driver -> "Item 1".equals(eventValue.getText()));

        clickItem("Item 1");
        waitUntil(driver -> eventValue.getText().isEmpty());
        Assert.assertEquals("client", eventOrigin.getText());
    }

    @Test
    public void clickClearButton_valuePropagatedToServer() {
        clickItem("Item 1");
        waitUntil(driver -> "Item 1".equals(eventValue.getText()));

        comboBox.$("[part~='clear-button']").first().click();

        waitUntil(driver -> eventValue.getText().isEmpty());
        Assert.assertEquals("client", eventOrigin.getText());
    }

    @Test
    public void removeChip_valuePropagatedToServer() {
        clickItem("Item 1");
        waitUntil(driver -> "Item 1".equals(eventValue.getText()));

        comboBox.$("vaadin-multi-select-combo-box-chip").get(1)
                .$("[part~='remove-button']").first().click();

        waitUntil(driver -> eventValue.getText().isEmpty());
        Assert.assertEquals("client", eventOrigin.getText());
    }

    @Test
    public void pressEscapeToClear_valuePropagatedToServer() {
        clickItem("Item 1");
        waitUntil(driver -> "Item 1".equals(eventValue.getText()));

        comboBox.sendKeys(Keys.ESCAPE);

        waitUntil(driver -> eventValue.getText().isEmpty());
        Assert.assertEquals("client", eventOrigin.getText());
    }

    private void clickItem(String label) {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        List<TestBenchElement> items = comboBox
                .$("vaadin-multi-select-combo-box-item").all();
        TestBenchElement item = items.stream()
                .filter(el -> label.equals(el.getText())).findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Item not found in dropdown: " + label));
        item.click();
        comboBox.closePopup();
    }
}
