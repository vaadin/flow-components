/*
 * Copyright 2000-2022 Vaadin Ltd.
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

@TestPath("vaadin-combo-box/client-side-data-range")
public class ComboBoxClientSideDataRangeIT extends AbstractComboBoxIT {
    private ComboBoxElement comboBox;

    private static final int MAX_LOADED_PAGES_COUNT = 10;

    @Before
    public void init() {
        open();
        comboBox = $(ComboBoxElement.class).first();
    }

    @Test
    public void defaultPageSize_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded() {
        scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(1000, 50);
    }

    @Test
    public void setSmallPageSize_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded() {
        $("input").id("set-page-size").sendKeys("10", Keys.ENTER);

        scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(1000, 10);
    }

    @Test
    public void setGreatPageSize_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded() {
        $("input").id("set-page-size").sendKeys("100", Keys.ENTER);

        scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(1000, 100);
    }

    @Test
    public void defaultPageSize_scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded() {
        scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(1000, 50);
    }

    @Test
    public void setSmallPageSize_scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded() {
        $("input").id("set-page-size").sendKeys("10", Keys.ENTER);

        scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(1000, 10);
    }

    @Test
    public void setGreatPageSize_scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded() {
        $("input").id("set-page-size").sendKeys("100", Keys.ENTER);

        scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(1000, 100);
    }

    private void scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(int itemsCount, int pageSize) {
        int pagesCount = itemsCount / pageSize;
        int maxLoadedItemsCount = MAX_LOADED_PAGES_COUNT * pageSize;

        comboBox.openPopup();

        // Scroll from the first page to the last page.
        for (int page = 0; page < pagesCount; page++) {
            int index = page * pageSize;
            scrollToItem(comboBox, index);
            waitUntilTextInContent("Item " + index);

            if (page < MAX_LOADED_PAGES_COUNT) {
                int loadedItemsCount = (page + 1) * pageSize;
                assertLoadedItemsCount(
                        String.format("Should have %s items loaded after scrolling to the page %s", loadedItemsCount, page),
                        loadedItemsCount,
                        comboBox);
            } else {
                assertLoadedItemsCount(
                    String.format("Should have %s items loaded after scrolling to the page %s", maxLoadedItemsCount,
                            page),
                    maxLoadedItemsCount,
                    comboBox);
            }
        }

        // Scroll from the last page to the first page.
        for (int page = pagesCount; page >= 0; page--) {
            int index = page * pageSize;
            scrollToItem(comboBox, index);
            waitUntilTextInContent("Item " + index);
            assertLoadedItemsCount(
                    String.format("Should have %s items loaded after scrolling back to the page %s", maxLoadedItemsCount,
                            page),
                    maxLoadedItemsCount,
                    comboBox);
        }
    }

    private void scrollToEnd_scrollBackAndForth_morePagesLoaded_overflowingPagesDiscarded(int itemsCount, int pageSize) {
        int pagesCount = itemsCount / pageSize;
        int maxLoadedItemsCount = MAX_LOADED_PAGES_COUNT * pageSize;

        comboBox.openPopup();

        // Scroll to end
        int lastIndex = itemsCount - 1;
        scrollToItem(comboBox, lastIndex);
        waitUntilTextInContent("Item " + lastIndex);
        assertLoadedItemsCount(String.format("Should have %s items loaded after scrolling to the end", pageSize), pageSize, comboBox);

        // Scroll from the last page to the first page.
        for (int page = pagesCount - 1; page >= 0; page--) {
            int index = page * pageSize;
            scrollToItem(comboBox, index);
            waitUntilTextInContent("Item " + index);

            if (pagesCount - page < MAX_LOADED_PAGES_COUNT) {
                int loadedItemsCount = (pagesCount - page) * pageSize;
                assertLoadedItemsCount(
                        String.format("Should have %s items loaded after scrolling to the page %s", loadedItemsCount, page),
                        loadedItemsCount,
                        comboBox);
            } else {
                assertLoadedItemsCount(
                    String.format("Should have %s items loaded after scrolling to the page %s", maxLoadedItemsCount,
                            page),
                    maxLoadedItemsCount,
                    comboBox);
            }
        }

        // Scroll from the first page to the last page.
        for (int page = 0; page <= pagesCount; page++) {
            int index = page * pageSize;
            scrollToItem(comboBox, index);
            waitUntilTextInContent("Item " + index);
            assertLoadedItemsCount(
                    String.format("Should have %s items loaded after scrolling to the page %s", maxLoadedItemsCount,
                            page),
                    maxLoadedItemsCount,
                    comboBox);
        }
    }
}
