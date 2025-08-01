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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

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
    private ComboBoxElement lazyWithSmallCustomPageSize;

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
        lazyWithSmallCustomPageSize = $(ComboBoxElement.class)
                .id("lazy-small-custom-page-size");
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
        assertRendered(stringBox, "Item 10");
    }

    @Test
    public void selectItem_changeFilter_properlyFilteredItems() {
        clickButton("set-value");
        stringBox.openPopup();

        stringBox.setFilter("Item 11");
        assertRendered(stringBox, "Item 11");
        assertNotRendered(stringBox, "Item 2");

        stringBox.setFilter("Item 111");
        assertRendered(stringBox, "Item 111");
        assertNotRendered(stringBox, "Item 2");
    }

    @Test
    public void clickItem_valueChanged() {
        stringBox.openPopup();
        getItemElements(stringBox).get(2).click();
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
        assertItemSelected(stringBox, "Item 10");
    }

    @Test
    public void setValueBeforeLoading_valueChanged_valueShown() {
        $("button").id("set-value").click();
        assertMessage("Item 10");
        Assert.assertEquals(
                "The selected value should be displayed in the ComboBox's TextField",
                "Item 10", getTextFieldValue(stringBox));
        stringBox.openPopup();
        // Make sure the item is in the viewport / rendered
        scrollToItem(stringBox, 10);
        assertItemSelected(stringBox, "Item 10");
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

        getItemElements(stringBox).get(4).click();
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
        waitUntilTextInContent(pagesizeBox, "Item 200");

        assertLoadedItemsCount("Expected two pages to be loaded.", 360,
                pagesizeBox);
        assertRendered(pagesizeBox, "Item 200");
    }

    @Test
    public void loadItems_changePageSize() {
        pagesizeBox.openPopup();
        scrollToItem(pagesizeBox, 180);

        clickButton("change-pagesize");
        pagesizeBox.openPopup();
        waitUntilTextInContent(pagesizeBox, "Item");
        assertLoadedItemsCount(
                "After opening the ComboBox, the first 'pageSize' amount "
                        + "of items should be loaded (with updated pageSize: 100).",
                100, pagesizeBox);

        scrollToItem(pagesizeBox, 100);

        assertLoadedItemsCount(
                "Expected two pages to be loaded (with updated pageSize 100).",
                200, pagesizeBox);
        assertRendered(pagesizeBox, "Item 100");
    }

    @Test
    public void loadItems_changeItemLabelGenerator() {
        beanBox.openPopup();
        clickButton("item-label-generator");
        beanBox.openPopup();
        assertRendered(beanBox, "Born 3");

        getItemElements(beanBox).get(5).click();
        Assert.assertEquals("Born 5", getTextFieldValue(beanBox));

        assertLoadedItemsCount("Only the first page should be loaded.", 50,
                beanBox);
    }

    @Test
    public void loadItems_changeRenderer() {
        beanBox.openPopup();
        clickButton("component-renderer");
        beanBox.openPopup();
        assertComponentRendered(beanBox, "<h4>Person 4</h4>");
        assertLoadedItemsCount("Only the first page should be loaded.", 50,
                beanBox);
    }

    @Test
    public void loadItems_changeDataProvider() {
        beanBox.openPopup();
        clickButton("data-provider");
        beanBox.openPopup();

        assertRendered(beanBox, "Changed 6");
        assertLoadedItemsCount("Only the first page should be loaded.", 50,
                beanBox);
    }

    @Test
    public void setItemLabelGenerator_setComponentRenderer_labelGeneratorUsedForTextField() {
        clickButton("item-label-generator");
        clickButton("component-renderer");
        beanBox.openPopup();
        assertComponentRendered(beanBox, "<h4>Person 4</h4>");
        getItemElements(beanBox).get(7).click();
        Assert.assertEquals("Born 7", getTextFieldValue(beanBox));

    }

    @Test
    public void loadItems_refreshItem_itemUpdated() {
        beanBox.openPopup();
        clickButton("update-item");
        beanBox.openPopup();
        Assert.assertEquals(
                "Expected the item to be updated after calling refreshItem().",
                "Updated", getOverlayContents(beanBox).get(0));
    }

    @Test
    public void loadItems_removeItem_itemRemoved() {
        beanBox.openPopup();
        clickButton("remove-item");
        beanBox.openPopup();
        assertNotRendered(beanBox, "Person 2");
        assertRendered(beanBox, "Person 1");
        assertRendered(beanBox, "Person 3");
    }

    @Test
    public void defaultFiltering_lowerCaseContains() {
        beanBox.openPopup();
        beanBox.setFilter("person 2");

        waitUntil(driver -> getNonEmptyOverlayContents(beanBox).size() > 5);

        getNonEmptyOverlayContents(beanBox).forEach(rendered -> {
            Assert.assertTrue(rendered.contains("Person 2"));
        });

        beanBox.setFilter("oN 330");

        waitUntil(driver -> getOverlayContents(beanBox).size() == 1);

        Assert.assertEquals("Unexpected item after filtering.", "Person 330",
                getNonEmptyOverlayContents(beanBox).get(0));
    }

    @Test
    public void customItemFilter() {
        filterBox.openPopup();
        waitForElementVisible(By.tagName("vaadin-combo-box-overlay"));

        filterBox.setFilter("Person");

        waitForElementNotPresent(By.tagName("vaadin-combo-box-overlay"));

        Assert.assertEquals(
                "None of the items should match the filter "
                        + "and overlay is not displayed",
                0, $("vaadin-combo-box-overlay").all().size());

        filterBox.setFilter("10");

        waitUntil(driver -> getNonEmptyOverlayContents(filterBox).size() > 0);

        getNonEmptyOverlayContents(filterBox).forEach(rendered -> {
            Assert.assertTrue("Expected rendered to contain 'Born: 10'",
                    rendered.contains("Born: 10"));
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
        assertRendered(callbackBox, "Item 0");

        // Now backend request should take place to init the data communicator
        Assert.assertEquals("1", lazySizeRequestCountSpan.getText());

        callbackBox.openPopup();
        scrollToItem(callbackBox, 60);

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                callbackBox);
        assertRendered(callbackBox, "Item 60");
    }

    @Test
    public void comboBoxInATemplate_worksWithLazyLoading() {
        templateBox.openPopup();

        assertLoadedItemsCount(
                "After opening the ComboBox, the first 50 items should be loaded",
                50, templateBox);
        assertRendered(templateBox, "Item 8");

        getItemElements(templateBox).get(8).click();
        assertMessage("Item 8");

        templateBox.openPopup();
        scrollToItem(templateBox, 50);

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                templateBox);
        assertRendered(templateBox, "Item 50");
    }

    @Test // https://github.com/vaadin/vaadin-combo-box-flow/issues/216
    public void filterAndSelectItemNotOnFirstPage_setCurrentValue_valueCorrect() {
        String item = "Item 151";

        stringBox.openPopup();
        stringBox.setFilter(item);
        waitUntil(driver -> getNonEmptyOverlayContents(stringBox).size() == 1);
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
        waitUntil(driver -> getNonEmptyOverlayContents(stringBox).size() == 1);
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
        waitUntilTextInContent(beanBox, "<h4>Person 300</h4>");

        scrollToItem(beanBox, 0);
        waitUntilTextInContent(beanBox, "<h4>Person 0</h4>");

        assertComponentRendered(beanBox, "<h4>Person 0</h4>");
        assertComponentRendered(beanBox, "<h4>Person 4</h4>");
        assertComponentRendered(beanBox, "<h4>Person 7</h4>");
    }

    @Test
    public void scrollDown_scrollUp_selectionRetained() {
        beanBox.sendKeys("Person 0");
        waitUntilTextInContent(beanBox, "Person 0");
        beanBox.sendKeys(Keys.ENTER);

        beanBox.openPopup();
        waitUntilTextInContent(beanBox, "Person 0");

        int scrollIndex = 600;
        scrollToItem(beanBox, scrollIndex);
        waitUntilTextInContent(beanBox, "Person " + scrollIndex);

        Assert.assertTrue(
                "First item should not be loaded after scrolling down enough",
                getLoadedItems(beanBox).size() < scrollIndex);

        scrollToItem(beanBox, 0);
        waitUntilTextInContent(beanBox, "Person 0");

        assertItemSelected(beanBox, "Person 0");
    }

    @Test
    public void filtering_filterRetained() {
        beanBox.sendKeys("Person 1");
        waitUntilTextInContent(beanBox, "Person 1");
        beanBox.sendKeys(Keys.ENTER);

        beanBox.sendKeys("11");
        waitUntilTextInContent(beanBox, "Person 111");

        String filterText = (String) executeScript(
                "return arguments[0].focusElement.value", beanBox);
        Assert.assertEquals("The ComboBox filter text got modified",
                "Person 111", filterText);
    }

    @Test
    public void customPageSize_pageSizePopulatedToDataCommunicator() {
        lazyCustomPageSize.openPopup();
        scrollToItem(lazyCustomPageSize, 100);
        waitUntilTextInContent(lazyCustomPageSize, "100");
        // page size should be 42
        assertMessage("42");

        clickButton("change-page-size-button");
        lazyCustomPageSize.closePopup();
        lazyCustomPageSize.openPopup();
        scrollToItem(lazyCustomPageSize, 300);
        waitUntilTextInContent(lazyCustomPageSize, "300");
        // page size should be 41
        assertMessage("41");
    }

    @Test
    public void disabledLazyLoading_reducePageSize_enablesLazyLoading() {
        disabledLazyLoadingBox.openPopup();
        assertLoadedItemsCount("Initially all 100 items should be loaded", 100,
                disabledLazyLoadingBox);
        disabledLazyLoadingBox.closePopup();

        clickButton("enable-lazy-loading");
        disabledLazyLoadingBox.openPopup();
        assertLoadedItemsCount(
                "After reducing page size, 50 items should be loaded", 50,
                disabledLazyLoadingBox);

        scrollToItem(disabledLazyLoadingBox, 100);
        assertLoadedItemsCount("Scrolling down should load further pages", 100,
                disabledLazyLoadingBox);
        assertRendered(disabledLazyLoadingBox, "99");
    }

    @Test
    // https://github.com/vaadin/flow-components/issues/3595
    public void smallCustomPageSize_filter_selectItem_loadingStateResolved() {
        String item = "2";

        lazyWithSmallCustomPageSize.openPopup();
        lazyWithSmallCustomPageSize.selectByText(item);
        lazyWithSmallCustomPageSize.setFilter(item);
        Assert.assertEquals(item,
                getSelectedItemLabel(lazyWithSmallCustomPageSize));

        lazyWithSmallCustomPageSize.closePopup();
        Assert.assertEquals(item,
                getSelectedItemLabel(lazyWithSmallCustomPageSize));

        lazyWithSmallCustomPageSize.click();
        assertLoadingStateResolved(lazyWithSmallCustomPageSize);
    }

    private void assertMessage(String expectedMessage) {
        Assert.assertEquals(expectedMessage, $("div").id("message").getText());
    }

}
