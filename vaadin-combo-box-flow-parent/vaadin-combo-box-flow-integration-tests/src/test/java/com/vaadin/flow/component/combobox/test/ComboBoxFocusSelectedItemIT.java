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
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/focus-selected-item")
public class ComboBoxFocusSelectedItemIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-combo-box"));
    }

    @Test
    public void lazyWithProvider_open_scrollsToSelected() {
        openAndAssertContains("lazy-with-provider", "Item 5000");
    }

    @Test
    public void lazyWithProvider_setValue_open_scrollsToNewSelected() {
        clickButton("lazy-with-provider-set-9000");
        openAndAssertContains("lazy-with-provider", "Item 9000");
    }

    @Test
    public void lazyWithProvider_reopen_stillScrollsToSelected() {
        ComboBoxElement combo = openAndAssertContains("lazy-with-provider",
                "Item 5000");
        combo.closePopup();
        openAndAssertContains("lazy-with-provider", "Item 5000");
    }

    @Test
    public void lazyWithProvider_clearValue_open_doesNotScroll() {
        clickButton("lazy-with-provider-clear");
        openAndAssertContains("lazy-with-provider", "Item 0");
    }

    @Test
    public void lazyWithProvider_toggleOff_open_doesNotScroll() {
        clickButton("lazy-with-provider-toggle-off");
        openAndAssertContains("lazy-with-provider", "Item 0");
    }

    @Test
    public void lazyWithProvider_filterActive_doesNotScrollToSelected() {
        ComboBoxElement combo = $(ComboBoxElement.class)
                .id("lazy-with-provider");
        combo.setFilter("5");
        assertLoadingStateResolved(combo);
        // First filtered item is "Item 5"; if the listener mistakenly ran,
        // the viewport would scroll deep into the filtered list.
        assertOverlayContains(combo, "Item 5");
    }

    @Test
    public void lazyToggleOff_runtimeToggleOn_open_scrollsToSelected() {
        clickButton("lazy-toggle-off-toggle-on");
        openAndAssertContains("lazy-toggle-off", "Item 5000");
    }

    @Test
    public void lazyToggleOff_detachReattach_open_scrollsToSelected() {
        clickButton("lazy-toggle-off-detach-reattach");
        openAndAssertContains("lazy-toggle-off", "Item 5000");
    }

    @Test
    public void inMemory_open_scrollsToSelected() {
        openAndAssertContains("in-memory", "Item 30");
    }

    @Test
    public void inMemory_filterActive_doesNotErrorOut() {
        ComboBoxElement combo = $(ComboBoxElement.class).id("in-memory");
        combo.setFilter("Item 1");
        assertLoadingStateResolved(combo);
        assertOverlayContains(combo, "Item 1");
    }

    @Test
    public void lazyToggleOff_open_doesNotScroll() {
        openAndAssertContains("lazy-toggle-off", "Item 0");
    }

    private ComboBoxElement openAndAssertContains(String id, String label) {
        ComboBoxElement combo = $(ComboBoxElement.class).id(id);
        combo.openPopup();
        assertLoadingStateResolved(combo);
        assertOverlayContains(combo, label);
        return combo;
    }

    private void assertOverlayContains(ComboBoxElement combo, String label) {
        waitUntilTextInContent(combo, label);
        List<String> contents = getOverlayContents(combo);
        Assert.assertTrue(
                "Overlay viewport should contain '" + label
                        + "'. Visible items: " + contents,
                contents.stream().anyMatch(label::equals));
    }
}
