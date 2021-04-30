/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/filtering")
public class FilteringIT extends AbstractComboBoxIT {

    private ComboBoxElement box;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        box = $(ComboBoxElement.class).first();
    }

    @Test
    public void itemsLessThanPageSize_clientSideFiltering() {
        box.openPopup();
        assertClientSideFilter(true);
    }

    @Test
    public void clientSideFiltering_lowerCaseContains() {
        box.openPopup();
        List<String> filteredItems = setFilterAndGetImmediateResults("item 20");
        Assert.assertEquals(
                "Expected one item to match the client-side filtering 'item 20'.",
                1, filteredItems.size());
        Assert.assertEquals("Unexpected item to match the filter.", "Item 20",
                filteredItems.get(0));

        filteredItems = setFilterAndGetImmediateResults("M 10");
        Assert.assertEquals(
                "Expected one item to match the client-side filtering 'M 10'.",
                1, filteredItems.size());
        Assert.assertEquals("Unexpected item to match the filter.", "Item 10",
                filteredItems.get(0));
    }

    @Test
    public void itemsMoreThanPageSize_serverSideFiltering() {
        clickButton("add-items");
        box.openPopup();
        assertClientSideFilter(false);
    }

    @Test
    public void loadItems_addItemsToCrossPageSize_switchToServerSideFiltering() {
        box.openPopup();
        clickButton("add-items");
        box.openPopup();
        assertRendered("Item 8");
        assertClientSideFilter(false);
    }

    @Test
    public void removeItemsToCrossPageSize_switchToClientSideFiltering() {
        clickButton("add-items");
        box.openPopup();
        clickButton("remove-items");
        box.openPopup();
        assertClientSideFilter(true);
    }

    @Test
    public void useItemFilterWithLessThanPageSize_serverSideFiltering() {
        clickButton("item-filter");
        box.openPopup();

        List<String> items = setFilterAndGetImmediateResults("tem 3");
        Assert.assertEquals("Expected server-side filtering, so there "
                + "should be no filtered items until server has responded.", 0,
                items.size());

        Assert.assertEquals("No item should match the ItemFilter (startsWith).",
                0, getNonEmptyOverlayContents().size());

        items = setFilterAndGetImmediateResults("Item 2");
        Assert.assertEquals("Expected server-side filtering, so there "
                + "should be no filtered items until server has responded.", 0,
                items.size());

        waitUntil(driver -> getNonEmptyOverlayContents().size() == 11);

        getNonEmptyOverlayContents().forEach(item -> Assert.assertThat(
                "Unexpected item found after filtering.", item,
                CoreMatchers.startsWith("Item 2")));
    }

    @Test
    public void biggerPageSize_clientSideFilteringWithMoreItems() {
        box = $(ComboBoxElement.class).id("page-size-60");

        clickButton("add-items");
        box.openPopup();
        assertClientSideFilter(true, "3", 15);

        clickButton("add-items");
        box.openPopup();
        assertClientSideFilter(false, "3", 17);
    }

    @Test
    public void changeFilterFromEmptyAndBackToEmptyWithinDebounceTimeout_itemsLoaded() {
        clickButton("add-items");
        box.openPopup();
        box.setFilter("0");
        box.setFilter("");
        waitUntil(driver -> getNonEmptyOverlayContents().size() > 0);
        assertRendered("Item 0");
    }

    @Test
    public void configureFilterInDataProvider_setDataProvider_serverSideFiltering() {
        box = $(ComboBoxElement.class).id("filterable-data-provider");
        box.openPopup();
        assertRendered("foo");
        List<String> filteredItems = setFilterAndGetImmediateResults("f");
        Assert.assertEquals("Expected server-side filtering, so there "
                + "should be no filtered items until server has responded.", 0,
                filteredItems.size());

        waitUntil(driver -> getNonEmptyOverlayContents().size() == 1);
        assertRendered("filtered");
    }

    @Test
    public void configureEmptyFilterToReturnNoItems_useCaseWorks() {
        box = $(ComboBoxElement.class).id("empty-filter-returns-none");
        box.openPopup();

        assertItemsNotLoaded();

        box.setFilter("foo");
        waitUntil(driver -> getNonEmptyOverlayContents().size() == 1);
        assertRendered("Item 0");

        box.setFilter("");
        assertItemsNotLoaded();
    }

    private void assertItemsNotLoaded() {
        try {
            waitUntil(driver -> {
                if (getLoadedItems(box).size() > 0) {
                    Assert.fail("Expected no items to be loaded when " +
                            "opening the ComboBox with empty filter.");
                }
                return false;
            }, 1);
        } catch (Exception e) {
            // Success
        }
    }

    private void assertClientSideFilter(boolean clientSide) {
        assertClientSideFilter(clientSide, "3", 13);
    }

    private void assertClientSideFilter(boolean clientSide, String filter,
            int expectedCount) {

        List<String> items = setFilterAndGetImmediateResults(filter);

        if (clientSide) {
            Assert.assertEquals("Unexpected amount of filtered items. "
                    + "Expected the items to be already filtered synchronously in client-side.",
                    expectedCount, items.size());
            items.forEach(item -> {
                Assert.assertThat(
                        "Found an item which doesn't match the filter. "
                                + "Expected the items to be already filtered synchronously in client-side.",
                        item, CoreMatchers.containsString(filter));
            });
        } else {
            Assert.assertEquals("Expected server-side filtering, so there "
                    + "should be no filtered items until server has responded.",
                    0, items.size());

            waitUntil(driver -> getNonEmptyOverlayContents().size() > 0);
            getNonEmptyOverlayContents().forEach(rendered -> {
                Assert.assertThat(
                        "Item which doesn't match the filter was found after server-side filtering.",
                        rendered, CoreMatchers.containsString(filter));
            });
        }
    }

    private List<String> setFilterAndGetImmediateResults(String filter) {
        /*
         * If combo box is doing client-side filtering, filteredItems is changed
         * synchronously after changing the filter.
         */
        String script = String.format("const box = arguments[0];" //
                + "box.filter = '%s';" //
                + "return box.filteredItems.map(item => item.label);", filter);
        return (List<String>) executeScript(script, box);
    }
}
