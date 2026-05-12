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

import static com.vaadin.flow.component.combobox.test.ComboBoxClientSideDataRangePage.ITEMS_COUNT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/client-side-data-range")
public class ComboBoxClientSideDataRangeIT extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).first();
    }

    @Test
    public void defaultPageSize_scrollUpAndDown_itemsRender_loadedCountBounded() {
        scrollUpAndDown_itemsRender_loadedCountBounded(50);
    }

    @Test
    public void defaultPageSize_scrollToEnd_scrollUpAndDown_itemsRender_loadedCountBounded() {
        scrollToEnd_scrollUpAndDown_itemsRender_loadedCountBounded(50);
    }

    @Test
    public void setGreatPageSize_scrollUpAndDown_itemsRender_loadedCountBounded() {
        $("input").id("set-page-size").sendKeys("300", Keys.ENTER);
        scrollUpAndDown_itemsRender_loadedCountBounded(300);
    }

    @Test
    public void setGreatPageSize_scrollToEnd_scrollUpAndDown_itemsRender_loadedCountBounded() {
        $("input").id("set-page-size").sendKeys("300", Keys.ENTER);
        scrollToEnd_scrollUpAndDown_itemsRender_loadedCountBounded(300);
    }

    private void scrollUpAndDown_itemsRender_loadedCountBounded(int pageSize) {
        comboBox.openPopup();

        // Scroll forward; each visited position renders its item.
        for (int i = 0; i < ITEMS_COUNT; i += pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent(comboBox, "Item " + i);
        }
        assertLoadedCountWithinBuffer(pageSize);

        // Scroll back; each position renders again after re-fetch.
        for (int i = ITEMS_COUNT - 1; i >= 0; i -= pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent(comboBox, "Item " + i);
        }
        assertLoadedCountWithinBuffer(pageSize);
    }

    private void scrollToEnd_scrollUpAndDown_itemsRender_loadedCountBounded(
            int pageSize) {
        comboBox.openPopup();

        int lastIndex = ITEMS_COUNT - 1;
        scrollToItem(comboBox, lastIndex);
        waitUntilTextInContent(comboBox, "Item " + lastIndex);
        assertLoadedCountWithinBuffer(pageSize);

        for (int i = lastIndex; i >= 0; i -= pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent(comboBox, "Item " + i);
        }
        assertLoadedCountWithinBuffer(pageSize);

        for (int i = 0; i < ITEMS_COUNT; i += pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent(comboBox, "Item " + i);
        }
        assertLoadedCountWithinBuffer(pageSize);
    }

    // The connector requests `viewport ± pageSize` so the active range
    // covers at most 4 pageSize-aligned pages around the current viewport.
    private void assertLoadedCountWithinBuffer(int pageSize) {
        int loaded = getLoadedItems(comboBox).size();
        Assert.assertTrue("Items should load after scrolling but was " + loaded,
                loaded > 0);
        Assert.assertTrue(
                "Loaded items should stay within the connector's buffer ("
                        + (pageSize * 4) + ") but was " + loaded,
                loaded <= pageSize * 4);
    }
}
