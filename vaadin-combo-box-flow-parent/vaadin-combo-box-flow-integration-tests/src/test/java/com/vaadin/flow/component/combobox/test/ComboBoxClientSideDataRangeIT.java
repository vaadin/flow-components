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
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

import static com.vaadin.flow.component.combobox.test.ComboBoxClientSideDataRangePage.ITEMS_COUNT;

@TestPath("vaadin-combo-box/client-side-data-range")
public class ComboBoxClientSideDataRangeIT extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).first();
    }

    @Test
    public void defaultPageSize_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded() {
        scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded(50, 500);
    }

    @Test
    public void defaultPageSize_scrollToEnd_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded() {
        scrollToEnd_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded(
                50, 500);
    }

    @Test
    public void setGreatPageSize_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded() {
        $("input").id("set-page-size").sendKeys("300", Keys.ENTER);

        scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded(300, 600);
    }

    @Test
    public void setGreatPageSize_scrollToEnd_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded() {
        $("input").id("set-page-size").sendKeys("300", Keys.ENTER);

        scrollToEnd_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded(
                300, 600);
    }

    private void scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded(
            int pageSize, int maxLoadedItemsCount) {
        comboBox.openPopup();

        // Scroll to the end page by page.
        for (int i = 0; i < ITEMS_COUNT; i += pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent("Item " + i);

            if (i < maxLoadedItemsCount) {
                int page = i / pageSize;
                int loadedItemsCount = (page + 1) * pageSize;
                assertLoadedItemsCount(String.format(
                        "Should have %s items loaded after scrolling to the index %s from the beginning",
                        loadedItemsCount, i), loadedItemsCount, comboBox);
            } else {
                assertLoadedItemsCount(String.format(
                        "Should have only %s items loaded after scrolling to the index %s from the beginning",
                        maxLoadedItemsCount, i), maxLoadedItemsCount, comboBox);
            }
        }

        // Scroll to the beginning page by page.
        for (int i = ITEMS_COUNT - 1; i >= 0; i -= pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent("Item " + i);
            assertLoadedItemsCount(String.format(
                    "Should have %s items loaded after scrolling to the index %s from the end",
                    maxLoadedItemsCount, i), maxLoadedItemsCount, comboBox);
        }
    }

    private void scrollToEnd_scrollUpAndDown_morePagesLoaded_overflowingPagesDiscarded(
            int pageSize, int maxLoadedItemsCount) {
        comboBox.openPopup();

        // Scroll to the end.
        int lastIndex = ITEMS_COUNT - 1;
        scrollToItem(comboBox, lastIndex);
        waitUntilTextInContent("Item " + lastIndex);
        assertLoadedItemsCount(String.format(
                "Should have %s items loaded after jumping to the end",
                pageSize), pageSize, comboBox);

        // Scroll to the beginning page by page.
        for (int i = lastIndex; i >= 0; i -= pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent("Item " + i);

            if (lastIndex - i < maxLoadedItemsCount) {
                int page = (lastIndex - i) / pageSize;
                int loadedItemsCount = (page + 1) * pageSize;
                assertLoadedItemsCount(String.format(
                        "Should have %s items loaded after scrolling to the index %s from the end",
                        loadedItemsCount, i), loadedItemsCount, comboBox);
            } else {
                assertLoadedItemsCount(String.format(
                        "Should have %s items loaded after scrolling to the index %s from the end",
                        maxLoadedItemsCount, i), maxLoadedItemsCount, comboBox);
            }
        }

        // Scroll to the end page by page.
        for (int i = 0; i < ITEMS_COUNT; i += pageSize) {
            scrollToItem(comboBox, i);
            waitUntilTextInContent("Item " + i);
            assertLoadedItemsCount(String.format(
                    "Should have %s items loaded after scrolling to the index %s from the beginning",
                    maxLoadedItemsCount, i), maxLoadedItemsCount, comboBox);
        }
    }
}
