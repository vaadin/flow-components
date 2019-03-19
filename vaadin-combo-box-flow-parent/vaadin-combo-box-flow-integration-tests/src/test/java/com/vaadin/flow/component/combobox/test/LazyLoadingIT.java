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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("lazy-loading")
public class LazyLoadingIT extends AbstractComboBoxIT {

    private ComboBoxElement stringBox;
    private ComboBoxElement pagesizeBox;
    private ComboBoxElement beanBox;
    private ComboBoxElement filterBox;
    private ComboBoxElement callbackBox;
    private ComboBoxElement templateBox;
    private ComboBoxElement emptyCallbackBox;

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
        stringBox = $(ComboBoxElement.class).id("lazy-strings");
        pagesizeBox = $(ComboBoxElement.class).id("pagesize");
        beanBox = $(ComboBoxElement.class).id("lazy-beans");
        filterBox = $(ComboBoxElement.class).id("custom-filter");
        callbackBox = $(ComboBoxElement.class).id("callback-dataprovider");
        templateBox = $("combo-box-in-a-template").id("template")
                .$(ComboBoxElement.class).first();
        emptyCallbackBox = $(ComboBoxElement.class).id("empty-callback");
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
    public void scrollOverlay_morePagesLoaded() {
        stringBox.openPopup();
        scrollToItem(stringBox, 50);

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                stringBox);
        assertRendered("Item 52");

        scrollToItem(stringBox, 100);

        assertLoadedItemsCount(
                "There should be 150 items after loading three pages", 150,
                stringBox);
        assertRendered("Item 115");
    }

    @Test
    public void openPopup_scrollToEnd_onlyFirstAndLastPagesLoaded() {
        stringBox.openPopup();
        scrollToItem(stringBox, 1000);
        assertLoadedItemsCount(
                "Expected the first and the last pages to be loaded (100 items).",
                100, stringBox);
        assertRendered("Item 999");
    }

    @Test
    public void scrollToEnd_scrollUpwards_pagesLoaded() {
        stringBox.openPopup();
        scrollToItem(stringBox, 1000);
        scrollToItem(stringBox, 920);

        assertLoadedItemsCount(
                "Expected the first and the two last pages to be loaded (150 items).",
                150, stringBox);
        assertRendered("Item 920");
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

        waitUntil(driver -> getNonEmptyOverlayContents().size() > 10);

        getNonEmptyOverlayContents().forEach(rendered -> {
            Assert.assertThat(rendered,
                    CoreMatchers.containsString("Person 2"));
        });

        beanBox.setFilter("oN 33");

        List<String> expectedFilteredItems = new ArrayList<>();
        expectedFilteredItems.add("Person 33");
        expectedFilteredItems.addAll(IntStream.range(0, 10)
                .mapToObj(i -> "Person 33" + i).collect(Collectors.toList()));

        waitUntil(driver -> getOverlayContents().size() == expectedFilteredItems
                .size());

        List<String> filteredItems = getNonEmptyOverlayContents();
        IntStream.range(0, filteredItems.size()).forEach(i -> {
            Assert.assertEquals("Unexpected item after filtering.",
                    expectedFilteredItems.get(i), filteredItems.get(i));
        });
    }

    @Test
    public void customItemFilter() {
        filterBox.openPopup();
        waitForElementVisible(By.tagName("vaadin-combo-box-overlay"));

        filterBox.setFilter("Person");

        Assert.assertEquals("None of the items should match the filter.", 0,
                getNonEmptyOverlayContents().size());

        filterBox.setFilter("10");

        waitUntil(driver -> getNonEmptyOverlayContents().size() > 5);

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
        waitUntil(driver -> !stringBox.getPropertyBoolean("loading"));
        assertLoadedItemsCount(
                "Expected no items to be loaded after setting "
                        + "a filter which doesn't match any item",
                0, stringBox);
    }

    @Test
    public void callbackDataProviderReturnsNoItems_openMultipleTimes_loadingStateResolved() {
        for (int i = 0; i < 3; i++) {
            emptyCallbackBox.openPopup();
            waitUntil(
                    driver -> !emptyCallbackBox.getPropertyBoolean("loading"));
            assertLoadedItemsCount("Expected no items to be loaded", 0,
                    emptyCallbackBox);
            emptyCallbackBox.closePopup();
        }
    }

    @Test
    public void callbackDataprovider_pagesLoadedLazily() {
        callbackBox.openPopup();
        assertLoadedItemsCount(
                "After opening the ComboBox, the first 50 items should be loaded",
                50, callbackBox);
        assertRendered("Item 10");

        callbackBox.openPopup();
        scrollToItem(callbackBox, 75);

        assertLoadedItemsCount(
                "There should be 100 items after loading two pages", 100,
                callbackBox);
        assertRendered("Item 70");
    }

    @Test
    public void comboBoxInATemplate_worksWithLazyLoading() {
        templateBox.openPopup();

        assertLoadedItemsCount(
                "After opening the ComboBox, the first 50 items should be loaded",
                50, templateBox);

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
    // https://github.com/vaadin/vaadin-combo-box-flow/issues/227
    // https://github.com/vaadin/vaadin-combo-box-flow/issues/232
    public void setComponentRenderer_scrollDown_scrollUp_itemsRendered() {
        clickButton("component-renderer");
        beanBox.openPopup();
        scrollToItem(beanBox, 300);
        scrollToItem(beanBox, 0);

        assertComponentRendered("<h4>Person 0</h4>");
        assertComponentRendered("<h4>Person 4</h4>");
        assertComponentRendered("<h4>Person 9</h4>");
    }

    private void assertMessage(String expectedMessage) {
        Assert.assertEquals(expectedMessage, $("div").id("message").getText());
    }

}
