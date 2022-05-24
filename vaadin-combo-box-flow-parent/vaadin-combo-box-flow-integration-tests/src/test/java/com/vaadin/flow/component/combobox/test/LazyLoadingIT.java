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

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-combo-box/lazy-loading")
public class LazyLoadingIT extends AbstractComboBoxIT {

    private ComboBoxElement stringBox;
    private ComboBoxElement stringBoxAutoOpenDisabled;
    private ComboBoxElement pagesizeBox;
    private ComboBoxElement beanBox;
    private ComboBoxElement filterBox;
    private ComboBoxElement callbackBox;
    private ComboBoxElement templateBox;
    private ComboBoxElement emptyCallbackBox;
    private ComboBoxElement lazyCustomPageSize;
    private ComboBoxElement disabledLazyLoadingBox;

    private WebElement lazySizeRequestCountSpan;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        stringBox = $(ComboBoxElement.class).id("lazy-strings");
        stringBoxAutoOpenDisabled = $(ComboBoxElement.class)
                .id("lazy-strings-autoopendisabled");
        pagesizeBox = $(ComboBoxElement.class).id("pagesize");
        beanBox = $(ComboBoxElement.class).id("lazy-beans");
        filterBox = $(ComboBoxElement.class).id("custom-filter");
        callbackBox = $(ComboBoxElement.class).id("callback-dataprovider");
        templateBox = $("combo-box-in-a-template").id("template")
                .$(ComboBoxElement.class).first();
        emptyCallbackBox = $(ComboBoxElement.class).id("empty-callback");
        lazyCustomPageSize = $(ComboBoxElement.class)
                .id("lazy-custom-page-size");
        lazySizeRequestCountSpan = findElement(
                By.id("callback-dataprovider-size-request-count"));
        disabledLazyLoadingBox = $(ComboBoxElement.class)
                .id("disabled-lazy-loading");
    }

    @Test
    public void initiallyEmpty() {
        assertLoadedItemsCount("Lazy loading ComboBox should not have items "
                + "before opening the dropdown.", 0, stringBox);
    }

    @Test
    public void openPopup_firstPageLoaded() {
        stringBox.openPopup();
        assertLoadedItemsCount(
                "After opening the ComboBox, the first 50 items should be loaded",
                50, stringBox);
        assertRendered("Item 10");
    }

    @Test
    public void selectItem_changeFilter_properlyFilteredItems() {
        clickButton("set-value");
        stringBox.openPopup();

        stringBox.setFilter("Item 11");
        assertRendered("Item 11");
        assertNotRendered("Item 2");

        stringBox.setFilter("Item 111");
        assertRendered("Item 111");
        assertNotRendered("Item 2");
    }

    @Test
    public void scrollOverlay_morePagesLoaded_overflowingPagesDiscarded() {
        stringBox.openPopup();
        scrollToItem(stringBox, 50);
        waitUntilTextInContent("Item 52");

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                stringBox);
        assertRendered("Item 52");

        scrollToItem(stringBox, 100);
        waitUntilTextInContent("Item 102");

        assertLoadedItemsCount(
                "There should be 150 items after loading three pages", 150,
                stringBox);
        assertRendered("Item 102");

        // The first pages should get discarded (active range has default limit
        // of 500)
        for (int i = 150; i <= 600; i += 50) {
            scrollToItem(stringBox, i);
            waitUntilTextInContent("Item " + i);
        }

        assertLoadedItemsCount(
                "There should be 500 items after loading multiple pages", 500,
                stringBox);
        assertRendered("Item 602");

        // The last pages should get discarded (active range has default limit
        // of 500)
        for (int i = 600; i >= 0; i -= 50) {
            scrollToItem(stringBox, i);
            waitUntilTextInContent("Item " + i);
        }

        assertLoadedItemsCount(
                "There should be 500 items after scrolling back to start", 500,
                stringBox);
        assertRendered("Item 2");
    }

    @Test
    public void openPopup_scrollToEnd_onlyLastPageLoaded() {
        stringBox.openPopup();
        scrollToItem(stringBox, 1000);
        waitUntil(e -> getOverlayContents().contains("Item 999"));
        assertLoadedItemsCount(
                "Expected the last page to be loaded (50 items).", 50,
                stringBox);
        assertRendered("Item 999");
    }

    @Test
    public void scrollToEnd_scrollUpwards_pagesLoaded() {
        stringBox.openPopup();
        scrollToItem(stringBox, 1000);
        waitUntilTextInContent("Item 999");
        scrollToItem(stringBox, 920);
        waitUntilTextInContent("Item 919");

        assertLoadedItemsCount(
                "Expected the two last pages to be loaded (100 items).", 100,
                stringBox);
        assertRendered("Item 920");

        scrollToItem(stringBox, 870);
        waitUntilTextInContent("Item 869");

        assertLoadedItemsCount(
                "Expected the three last pages to be loaded (150 items).", 150,
                stringBox);
        assertRendered("Item 870");
        assertNotRendered("Item 990");

    }

    @Test
    public void clickItem_valueChanged() {
        stringBox.openPopup();
        getItemElements().get(2).click();
        assertMessage("Item 2");
    }

    @Test
    public void openPopup_setValue_valueChanged_valueShown() {
        stringBox.openPopup();
        clickButton("set-value");
        assertMessage("Item 10");
        Assert.assertEquals(
                "The selected value should be displayed in the ComboBox's TextField",
                "Item 10", getTextFieldValue(stringBox));
        stringBox.openPopup();
        assertItemSelected("Item 10");
    }

    @Test
    public void setValueBeforeLoading_valueChanged_valueShown() {
        $("button").id("set-value").click();
        assertMessage("Item 10");
        Assert.assertEquals(
                "The selected value should be displayed in the ComboBox's TextField",
                "Item 10", getTextFieldValue(stringBox));
        stringBox.openPopup();
        assertItemSelected("Item 10");
    }

    @Test
    public void setDisabled_removeDisabledAttribute_noDataLoaded() {
        clickButton("disable");
        removeDisabledAttribute(stringBox);

        stringBox.openPopup();
        assertLoadedItemsCount(
                "No items should be loaded for disabled ComboBox.", 0,
                stringBox);

        scrollToItem(stringBox, 60);
        assertLoadedItemsCount(
                "No items should be loaded for disabled ComboBox.", 0,
                stringBox);
    }

    @Test
    public void openPopup_setDisabled_removeDisabledAttribute_noNewDataLoaded() {
        stringBox.openPopup();
        clickButton("disable");
        removeDisabledAttribute(stringBox);

        stringBox.openPopup();
        scrollToItem(stringBox, 60);
        assertLoadedItemsCount(
                "No new items should be loaded for disabled ComboBox.", 50,
                stringBox);
    }

    @Test
    public void setDisabled_removeDisabledAttribute_clickItem_noEvent() {
        stringBox.openPopup();
        clickButton("disable");
        removeDisabledAttribute(stringBox);
        stringBox.openPopup();

        getItemElements().get(4).click();
        assertMessage("");
    }

    @Test
    public void customPageSize_correctAmountOfItemsRequested() {
        pagesizeBox.openPopup();
        assertLoadedItemsCount(
                "After opening the ComboBox, the first 'pageSize' amount "
                        + "of items should be loaded.",
                180, pagesizeBox);

        scrollToItem(pagesizeBox, 200);
        waitUntilTextInContent("Item 200");

        assertLoadedItemsCount("Expected two pages to be loaded.", 360,
                pagesizeBox);
        assertRendered("Item 200");
    }

    @Test
    public void loadItems_changePageSize() {
        pagesizeBox.openPopup();
        scrollToItem(pagesizeBox, 180);

        clickButton("change-pagesize");
        pagesizeBox.openPopup();
        waitUntilTextInContent("Item");
        assertLoadedItemsCount(
                "After opening the ComboBox, the first 'pageSize' amount "
                        + "of items should be loaded (with updated pageSize: 100).",
                100, pagesizeBox);

        scrollToItem(pagesizeBox, 100);

        assertLoadedItemsCount(
                "Expected two pages to be loaded (with updated pageSize 100).",
                200, pagesizeBox);
        assertRendered("Item 100");
    }

    @Test
    public void loadItems_changeItemLabelGenerator() {
        beanBox.openPopup();
        clickButton("item-label-generator");
        beanBox.openPopup();
        assertRendered("Born 3");

        getItemElements().get(5).click();
        Assert.assertEquals("Born 5", getTextFieldValue(beanBox));

        assertLoadedItemsCount("Only the first page should be loaded.", 50,
                beanBox);
    }

    @Test
    public void loadItems_changeRenderer() {
        beanBox.openPopup();
        clickButton("component-renderer");
        beanBox.openPopup();
        assertComponentRendered("<h4>Person 4</h4>");
        assertLoadedItemsCount("Only the first page should be loaded.", 50,
                beanBox);
    }

    @Test
    public void loadItems_changeDataProvider() {
        beanBox.openPopup();
        clickButton("data-provider");
        beanBox.openPopup();

        assertRendered("Changed 6");
        assertLoadedItemsCount("Only the first page should be loaded.", 50,
                beanBox);
    }

    @Test
    public void setItemLabelGenerator_setComponentRenderer_labelGeneratorUsedForTextField() {
        clickButton("item-label-generator");
        clickButton("component-renderer");
        beanBox.openPopup();
        assertComponentRendered("<h4>Person 4</h4>");
        getItemElements().get(7).click();
        Assert.assertEquals("Born 7", getTextFieldValue(beanBox));

    }

    @Test
    public void loadItems_refreshItem_itemUpdated() {
        beanBox.openPopup();
        clickButton("update-item");
        beanBox.openPopup();
        Assert.assertEquals(
                "Expected the item to be updated after calling refreshItem().",
                "Updated", getOverlayContents().get(0));
    }

    @Test
    public void loadItems_removeItem_itemRemoved() {
        beanBox.openPopup();
        clickButton("remove-item");
        beanBox.openPopup();
        assertNotRendered("Person 2");
        assertRendered("Person 1");
        assertRendered("Person 3");
    }

    @Test
    public void defaultFiltering_lowerCaseContains() {
        beanBox.openPopup();
        beanBox.setFilter("person 2");

        waitUntil(driver -> getNonEmptyOverlayContents().size() > 5);

        getNonEmptyOverlayContents().forEach(rendered -> {
            Assert.assertTrue(rendered.contains("Person 2"));
        });

        beanBox.setFilter("oN 330");

        waitUntil(driver -> getOverlayContents().size() == 1);

        Assert.assertEquals("Unexpected item after filtering.", "Person 330",
                getNonEmptyOverlayContents().get(0));
    }

    @Test
    public void customItemFilter() {
        filterBox.openPopup();
        waitForElementVisible(By.tagName("vaadin-combo-box-overlay"));

        filterBox.setFilter("Person");

        Assert.assertEquals(
                "None of the items should match the filter "
                        + "and overlay is not displayed",
                0, $("vaadin-combo-box-overlay").all().size());

        filterBox.setFilter("10");

        waitUntil(driver -> getNonEmptyOverlayContents().size() > 0);

        getNonEmptyOverlayContents().forEach(rendered -> {
            Assert.assertThat(rendered,
                    CoreMatchers.containsString("Born: 10"));
        });
    }

    @Test
    public void filterMatchesNoItems_loadingStateResolved() {
        // Otherwise the spinner is not cleared and it looks like the web
        // component is still waiting for more data.
        stringBox.openPopup();
        stringBox.setFilter("foo");
        assertLoadingStateResolved(stringBox);
        assertLoadedItemsCount(
                "Expected no items to be loaded after setting "
                        + "a filter which doesn't match any item",
                0, stringBox);
    }

    @Test
    public void callbackDataProviderReturnsNoItems_openMultipleTimes_loadingStateResolved() {
        for (int i = 0; i < 3; i++) {
            emptyCallbackBox.openPopup();
            assertLoadingStateResolved(callbackBox);
            assertLoadedItemsCount("Expected no items to be loaded", 0,
                    emptyCallbackBox);
            emptyCallbackBox.closePopup();
        }
    }

    @Test
    public void callbackDataprovider_pagesLoadedLazily() {
        // Check that no backend calls before open popup
        Assert.assertEquals("0", lazySizeRequestCountSpan.getText());

        callbackBox.openPopup();
        assertLoadedItemsCount(
                "After opening the ComboBox, the first 50 items should be loaded",
                50, callbackBox);
        assertRendered("Item 10");

        // Now backend request should take place to init the data communicator
        Assert.assertEquals("1", lazySizeRequestCountSpan.getText());

        callbackBox.openPopup();
        scrollToItem(callbackBox, 60);

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                callbackBox);
        assertRendered("Item 58");
    }

    @Test
    public void comboBoxInATemplate_worksWithLazyLoading() {
        templateBox.openPopup();

        assertLoadedItemsCount(
                "After opening the ComboBox, the first 50 items should be loaded",
                50, templateBox);
        assertRendered("Item 8");

        getItemElements().get(8).click();
        assertMessage("Item 8");

        templateBox.openPopup();
        scrollToItem(templateBox, 50);

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                templateBox);
        assertRendered("Item 52");
    }

    @Test // https://github.com/vaadin/vaadin-combo-box-flow/issues/216
    public void filterAndSelectItemNotOnFirstPage_setCurrentValue_valueCorrect() {
        String item = "Item 151";

        stringBox.openPopup();
        stringBox.setFilter(item);
        waitUntil(driver -> getNonEmptyOverlayContents().size() == 1);
        stringBox.selectByText(item);

        clickButton("set-current-value");

        assertMessage(item);
        Assert.assertEquals(item, getSelectedItemLabel(stringBox));
    }

    @Test
    public void autoOpenDisabled_setValue_valueChanged() {
        String item = "Item 151";
        stringBox.openPopup();
        stringBox.setFilter(item);
        waitUntil(driver -> getNonEmptyOverlayContents().size() == 1);
        stringBoxAutoOpenDisabled.selectByText(item);
        assertMessage(item);
        Assert.assertEquals(item,
                getSelectedItemLabel(stringBoxAutoOpenDisabled));
        Assert.assertFalse(stringBoxAutoOpenDisabled.isAutoOpen());
    }

    @Test
    // https://github.com/vaadin/vaadin-combo-box-flow/issues/227
    // https://github.com/vaadin/vaadin-combo-box-flow/issues/232
    public void setComponentRenderer_scrollDown_scrollUp_itemsRendered() {
        clickButton("component-renderer");
        beanBox.openPopup();

        scrollToItem(beanBox, 300);
        waitUntilTextInContent("<h4>Person 300</h4>");

        scrollToItem(beanBox, 0);
        waitUntilTextInContent("<h4>Person 0</h4>");

        assertComponentRendered("<h4>Person 0</h4>");
        assertComponentRendered("<h4>Person 4</h4>");
        assertComponentRendered("<h4>Person 7</h4>");
    }

    @Test
    public void scrollDown_scrollUp_selectionRetained() {
        beanBox.sendKeys("Person 0");
        waitUntilTextInContent("Person 0");
        beanBox.sendKeys(Keys.ENTER);

        beanBox.openPopup();
        waitUntilTextInContent("Person 0");

        int scrollIndex = 600;
        scrollToItem(beanBox, scrollIndex);
        waitUntilTextInContent("Person " + scrollIndex);

        Assert.assertTrue(
                "First item should not be loaded after scrolling down enough",
                getLoadedItems(beanBox).size() < scrollIndex);

        scrollToItem(beanBox, 0);
        waitUntilTextInContent("Person 0");

        assertItemSelected("Person 0");
    }

    @Test
    public void filtering_filterRetained() {
        beanBox.sendKeys("Person 1");
        waitUntilTextInContent("Person 1");
        beanBox.sendKeys(Keys.ENTER);

        beanBox.sendKeys("11");
        waitUntilTextInContent("Person 111");

        String filterText = (String) executeScript(
                "return arguments[0].focusElement.value", beanBox);
        Assert.assertEquals("The ComboBox filter text got modified",
                "Person 111", filterText);
    }

    @Test
    public void customPageSize_pageSizePopulatedToDataCommunicator() {
        lazyCustomPageSize.openPopup();
        scrollToItem(lazyCustomPageSize, 100);
        waitUntilTextInContent("100");
        // page size should be 42
        assertMessage("42");

        clickButton("change-page-size-button");
        lazyCustomPageSize.closePopup();
        lazyCustomPageSize.openPopup();
        scrollToItem(lazyCustomPageSize, 300);
        waitUntilTextInContent("300");
        // page size should be 41
        assertMessage("41");
    }

    @Test
    public void disabledLazyLoading_reducePageSize_enablesLazyLoading() {
        disabledLazyLoadingBox.openPopup();
        assertLoadedItemsCount("Initially all 100 items should be loaded", 100,
                disabledLazyLoadingBox);
        lazyCustomPageSize.closePopup();

        clickButton("enable-lazy-loading");
        disabledLazyLoadingBox.openPopup();
        assertLoadedItemsCount(
                "After reducing page size, 50 items should be loaded", 50,
                disabledLazyLoadingBox);

        scrollToItem(disabledLazyLoadingBox, 100);
        assertLoadedItemsCount("Scrolling down should load further pages", 100,
                disabledLazyLoadingBox);
        assertRendered("99");
    }

    private void assertMessage(String expectedMessage) {
        Assert.assertEquals(expectedMessage, $("div").id("message").getText());
    }

}
