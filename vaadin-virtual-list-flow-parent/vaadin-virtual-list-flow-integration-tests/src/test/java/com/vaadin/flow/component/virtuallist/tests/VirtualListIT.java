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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonNull;
import elemental.json.JsonObject;

@TestPath("vaadin-virtual-list/virtual-list-test")
public class VirtualListIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        WebElement loadingIndicator = findElement(
                By.className("v-loading-indicator"));
        waitUntil(driver -> !loadingIndicator.isDisplayed()
                && findElements(By.tagName("vaadin-virtual-list")).size() > 0);
    }

    @Test
    public void listWithStrings() {
        String listId = "list-with-strings";

        testInitialLoadOfItems(listId, "Item ");
        clickToSet2Items_listIstUpdated(listId, "list-with-strings-2-items",
                "Another item ");
        clickToSet3Items_listIsUpdated(listId, "list-with-strings-3-items",
                "Item ");
        clickToSet0Items_listIsUpdated(listId, "list-with-strings-0-items");
    }

    @Test
    public void dataProviderWithStrings() {
        String listId = "dataprovider-with-strings";

        testInitialLoadOfItems(listId, "Item ");
        clickToSet2Items_listIstUpdated(listId,
                "dataprovider-with-strings-2-items", "Another item ");
        clickToSet3Items_listIsUpdated(listId,
                "dataprovider-with-strings-3-items", "Item ");
        clickToSet0Items_listIsUpdated(listId,
                "dataprovider-with-strings-0-items");
    }

    @Test
    public void templateFromValueProviderWithPeople() {
        String listId = "dataprovider-with-people";

        testInitialLoadOfItems(listId, "Person ");
        clickToSet2Items_listIstUpdated(listId,
                "dataprovider-with-people-2-items", "");
        clickToSet3Items_listIsUpdated(listId,
                "dataprovider-with-people-3-items", "Person ");
        clickToSet0Items_listIsUpdated(listId,
                "dataprovider-with-people-0-items");
    }

    @Test
    public void templateFromRendererWithPeople() {
        WebElement list = findElement(By.id("template-renderer-with-people"));

        JsonArray items = getItems(getDriver(), list);
        Assert.assertEquals(3, items.length());
        for (int i = 0; i < items.length(); i++) {
            Assert.assertEquals(String.valueOf(i + 1),
                    items.getObject(i).getString("key"));
            Assert.assertEquals("Person " + (i + 1),
                    items.getObject(i).getString("name"));
            Assert.assertEquals(String.valueOf(i + 1),
                    items.getObject(i).getString("age"));
            Assert.assertEquals("person_" + (i + 1),
                    items.getObject(i).getString("user"));
        }

        WebElement update = findElement(
                By.id("template-renderer-with-people-update-item"));

        scrollIntoViewAndClick(update);
        items = getItems(getDriver(), list);
        JsonObject person = items.getObject(0);
        Assert.assertEquals("Person 1 Updated", person.getString("name"));
        Assert.assertEquals("person_1_updated", person.getString("user"));
    }

    @Test
    public void lazyLoaded() {
        WebElement list = findElement(By.id("lazy-loaded"));

        JsonArray items = getItems(getDriver(), list);
        // the items are preallocated in the list, but they are empty
        Assert.assertEquals(100, items.length());

        // Last received index
        int lastReceivedKey = 28;
        assertItemsArePresent(items, 0, lastReceivedKey, "Item ");

        // all the remaining items should be empty
        for (int i = lastReceivedKey; i < items.length(); i++) {
            MatcherAssert.assertThat(items.get(i),
                    CoreMatchers.instanceOf(JsonNull.class));
        }

        scrollToBottom(list);
        waitUntil(driver -> getItems(driver, list).get(0) instanceof JsonNull);

        items = getItems(getDriver(), list);

        // all the initial items should be empty
        assertItemsAreNotPresent(items, 0, items.length() - lastReceivedKey);

        // the last [lastReceivedKey] items should have data
        assertItemsArePresent(items, items.length() - lastReceivedKey,
                items.length(), "Item ");
    }

    @Test
    public void firstVisibleRowIndex() {
        VirtualListElement list = $(VirtualListElement.class)
                .id("component-renderer");
        Assert.assertEquals(0, list.getFirstVisibleRowIndex());
    }

    @Test
    public void lastVisibleRowIndex() {
        VirtualListElement list = $(VirtualListElement.class)
                .id("component-renderer");
        Assert.assertEquals(5, list.getLastVisibleRowIndex());
    }

    @Test
    public void rowIndexInView() {
        VirtualListElement list = $(VirtualListElement.class)
                .id("component-renderer");
        Assert.assertTrue(list.isRowInView(3));
    }

    @Test
    public void rowIndexNotInView() {
        VirtualListElement list = $(VirtualListElement.class)
                .id("component-renderer");
        Assert.assertFalse(list.isRowInView(6));
    }

    @Test
    public void rowCount() {
        VirtualListElement list = $(VirtualListElement.class)
                .id("component-renderer");
        Assert.assertEquals(100, list.getRowCount());
    }

    @Test
    public void templateWithEventHandlers() {
        WebElement list = findElement(By.id("template-events"));
        WebElement message = findElement(By.id("template-events-message"));

        JsonArray items = getItems(driver, list);
        assertItemsArePresent(items, 0, 3, "Clickable item ");

        // clicks on the first item to remove it
        WebElement item = findElement(By.id("template-events-item-0"));
        scrollIntoViewAndClick(item);
        waitUntil(driver -> getItems(driver, list).length() == 2);
        Assert.assertEquals("Clickable item 1 removed", message.getText());

        // clicks on the last item to remove it
        item = findElement(By.id("template-events-item-1"));
        scrollIntoViewAndClick(item);
        waitUntil(driver -> getItems(driver, list).length() == 1);
        Assert.assertEquals("Clickable item 3 removed", message.getText());

        // clicks on the first item again to remove it
        item = findElement(By.id("template-events-item-0"));
        scrollIntoViewAndClick(item);
        waitUntil(driver -> getItems(driver, list).length() == 0);
        Assert.assertEquals("Clickable item 2 removed", message.getText());
    }

    @Test
    public void listWithComponentRenderer() {
        WebElement list = findElement(By.id("component-renderer"));

        List<WebElement> items = list
                .findElements(By.className("component-rendered"));

        assertListContainsMaxItems(items.size(), 25);

        for (int i = 0; i < items.size(); i++) {
            WebElement item = items.get(i);
            Assert.assertEquals("div", item.getTagName());
            Assert.assertEquals("Item " + (i + 1),
                    item.getAttribute("innerHTML"));
        }

        scrollToBottom(list);

        waitUntil(
                driver -> list.getAttribute("innerHTML").contains("Item 100"));

        items = list.findElements(By.className("component-rendered"));

        assertListContainsMaxItems(items.size(), 25);
    }

    @Test

    public void listWithComponentRendererWithBeansAndPlaceholder_scrollToBottom_placeholderIsShown() {
        WebElement list = findElement(By.id("component-renderer-with-beans"));
        List<WebElement> items = list
                .findElements(By.className("component-rendered"));

        assertListContainsMaxItems(items.size(), 25);

        MatcherAssert.assertThat(list.getAttribute("innerText"), CoreMatchers
                .not(CoreMatchers.containsString("the-placeholder")));

        // Scroll to bottom and set an attribute when a placeholder becomes
        // visible.
        executeScript(
        //@formatter:off
            "let virtualList = arguments[0];"
          + "virtualList.scrollBy(0,10000);"
          + "let count = 0;"
          + "function isPlaceholderVisible() {"
          + "  placeholderVisible = virtualList.innerText.indexOf('the-placeholder') >= 0;"
          + "  count++;"
          + "  if(placeholderVisible) {"
          + "    virtualList.setAttribute('placeholderWasHere', 'true');"
          + "  }"
          + "  else if(count < 30) {"
          + "    setTimeout(isPlaceholderVisible, 20);"
          + "  }"
          + "}"
          + "isPlaceholderVisible();",
        //@formatter:on
                list);

        waitUntil(driver -> "true"
                .equals(list.getAttribute("placeholderWasHere")));

        waitUntil(driver -> list.getAttribute("innerText")
                .contains("Person 100"));

        MatcherAssert.assertThat(
                "The VirtualList shouldn't display any placeholders after the data is loaded",
                list.getAttribute("innerText"), CoreMatchers
                        .not(CoreMatchers.containsString("the-placeholder")));

        assertListContainsMaxItems(items.size(), 25);
    }

    private void assertListContainsMaxItems(int numOfItems, int maxItems) {
        Assert.assertTrue(String.format(
                "VirtualList shouldn't load this many items at once. "
                        + "Expected at most %s, but got %s.",
                maxItems, numOfItems), numOfItems <= maxItems);
    }

    @Test
    public void detachableList_changeContainers_itemsAreStillShown() {
        WebElement container1 = findElement(
                By.id("detachable-list-container-1"));
        WebElement container2 = findElement(
                By.id("detachable-list-container-2"));
        WebElement attach1 = findElement(By.id("detachable-list-attach-1"));
        WebElement attach2 = findElement(By.id("detachable-list-attach-2"));

        WebElement list = container1.findElement(By.id("detachable-list"));
        assertItemsArePresent(list, 20);

        // sets a property on the $connector, to validate that the connector
        // is not reset when changing containers
        executeScript("arguments[0].$connector._isUsingTheSameInstance = true",
                list);

        attach2.click();
        list = container2.findElement(By.id("detachable-list"));
        assertItemsArePresent(list, 20);
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        list));

        attach1.click();
        list = container1.findElement(By.id("detachable-list"));
        assertItemsArePresent(list, 20);
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        list));
    }

    @Test
    public void detachableList_detachAndReattach_itemsAreStillShown() {
        WebElement container1 = findElement(
                By.id("detachable-list-container-1"));
        WebElement attach1 = findElement(By.id("detachable-list-attach-1"));
        WebElement detach = findElement(By.id("detachable-list-detach"));

        WebElement list = container1.findElement(By.id("detachable-list"));
        scrollToElement(list);

        assertItemsArePresent(list, 20);

        detach.click();
        waitForElementNotPresent(By.id("detachable-list"));
        attach1.click();
        list = container1.findElement(By.id("detachable-list"));
        assertItemsArePresent(list, 20);
    }

    @Test
    public void detachableList_setInvisibleAndVisible_itemsAreStillShown() {
        WebElement container1 = findElement(
                By.id("detachable-list-container-1"));
        WebElement invisible = findElement(By.id("detachable-list-invisible"));
        WebElement visible = findElement(By.id("detachable-list-visible"));

        WebElement list = container1.findElement(By.id("detachable-list"));
        scrollToElement(list);

        assertItemsArePresent(list, 20);
        // sets a property on the $connector, to validate that the connector
        // is not reset when changing visibility
        executeScript("arguments[0].$connector._isUsingTheSameInstance = true",
                list);

        invisible.click();
        waitUntil(driver -> "true".equals(list.getAttribute("hidden")));
        visible.click();
        waitUntil(driver -> list.getAttribute("hidden") == null);
        assertItemsArePresent(list, 20);
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        list));
    }

    @Test
    public void nativeButtonRenderer() {
        List<TestBenchElement> buttons = $("vaadin-virtual-list")
                .id("list-with-buttons").$("button").all();
        Assert.assertEquals(3, buttons.size());
        IntStream.range(0, 3).forEach(i -> {
            Assert.assertEquals("Person " + (i + 1), buttons.get(i).getText());
        });
    }

    @Test
    public void numberRenderer() {
        List<TestBenchElement> items = $("vaadin-virtual-list")
                .id("list-with-numbers")
                .$("div[style*=\"position: absolute;\"]").all();
        Assert.assertEquals(3, items.size());
        IntStream.range(0, 3).forEach(i -> {
            Assert.assertEquals("" + (i + 1), items.get(i).getText());
        });
    }

    @Test
    public void localDateRenderer() {
        List<TestBenchElement> items = $("vaadin-virtual-list")
                .id("list-with-local-dates")
                .$("div[style*=\"position: absolute;\"]").all();
        Assert.assertEquals(3, items.size());

        Assert.assertEquals("January 1, 2001", items.get(0).getText());
        Assert.assertEquals("February 2, 2002", items.get(1).getText());
        Assert.assertEquals("March 3, 2003", items.get(2).getText());
    }

    @Test
    public void localDateTimeRenderer() {
        List<TestBenchElement> items = $("vaadin-virtual-list")
                .id("list-with-local-date-times")
                .$("div[style*=\"position: absolute;\"]").all();
        Assert.assertEquals(3, items.size());

        // JDK16 adds extra comma after year in en_US
        Assert.assertTrue(
                items.get(0).getText().matches("January 1, 2001,? 1:01 AM"));
        Assert.assertTrue(
                items.get(1).getText().matches("February 2, 2002,? 2:02 AM"));
        Assert.assertTrue(
                items.get(2).getText().matches("March 3, 2003,? 3:03 AM"));
    }

    @Test
    public void virtualListInsideFlexContainer_hasNonZeroWidthAndHeight() {
        TestBenchElement virtualList = $("vaadin-virtual-list")
                .id("list-inside-flex-container");

        assertWidthAndHeightInFlexContainer(virtualList);

        $("button").id("set-flex-direction-column").click();
        assertWidthAndHeightInFlexContainer(virtualList);
    }

    private void assertWidthAndHeightInFlexContainer(
            TestBenchElement virtualList) {
        Assert.assertTrue(
                "VirtualList should not have zero width by default "
                        + "when used inside a flex container.",
                virtualList.getPropertyInteger("clientWidth") > 0);
        Assert.assertTrue(
                "VirtualList should not have zero height by default "
                        + "when used inside a flex container.",
                virtualList.getPropertyInteger("clientHeight") > 0);
    }

    private void scrollToBottom(WebElement virtualList) {
        executeScript("arguments[0].scrollBy(0,10000);", virtualList);
    }

    private void assertItemsArePresent(WebElement list, int length) {
        JsonArray items = getItems(driver, list);
        Assert.assertEquals(length, items.length());
        for (int i = 0; i < items.length(); i++) {
            JsonObject obj = items.getObject(i);
            Assert.assertEquals("Person " + (i + 1), obj.getString("label"));
        }
    }

    private void assertItemsArePresent(JsonArray items, int startingIndex,
            int endingIndex, String itemLabelprefix) {

        for (int i = startingIndex; i < endingIndex; i++) {
            MatcherAssert.assertThat(
                    "Object at index " + i + " is null, when it shouldn't be",
                    items.get(i),
                    CoreMatchers.not(CoreMatchers.instanceOf(JsonNull.class)));
            Assert.assertEquals(itemLabelprefix + (i + 1),
                    items.getObject(i).getString("label"));
        }
    }

    private void assertItemsAreNotPresent(JsonArray items, int startingIndex,
            int endingIndex) {

        for (int i = startingIndex; i < endingIndex; i++) {
            MatcherAssert.assertThat(
                    "Object at index " + i + " is not null, when it should be",
                    items.get(i), CoreMatchers.instanceOf(JsonNull.class));
        }
    }

    private void testInitialLoadOfItems(String listId,
            String itemLabelPrefixForFirstSet) {
        WebElement list = findElement(By.id(listId));

        JsonArray items = getItems(getDriver(), list);
        Assert.assertEquals(3, items.length());

        assertItemsArePresent(items, 0, 3, itemLabelPrefixForFirstSet);
    }

    private void clickToSet2Items_listIstUpdated(String listId,
            String buttonIdFor2Items, String itemLabelPrefixForSecondSet) {
        WebElement list = findElement(By.id(listId));

        WebElement set2Items = findElement(By.id(buttonIdFor2Items));

        scrollIntoViewAndClick(set2Items);
        waitUntil(driver -> getItems(driver, list).length() == 2);
        JsonArray items = getItems(getDriver(), list);
        for (int i = 0; i < items.length(); i++) {
            Assert.assertEquals(
                    "The label of the initial object at the index " + i
                            + " of the list '" + listId + "' is wrong",
                    itemLabelPrefixForSecondSet + (i + 1),
                    items.getObject(i).getString("label"));
        }
    }

    private void clickToSet3Items_listIsUpdated(String listId,
            String buttonIdFor3Items, String itemLabelPrefixForFirstSet) {
        WebElement list = findElement(By.id(listId));

        WebElement set3Items = findElement(By.id(buttonIdFor3Items));

        scrollIntoViewAndClick(set3Items);
        waitUntil(driver -> getItems(driver, list).length() == 3);
        JsonArray items = getItems(getDriver(), list);
        for (int i = 0; i < items.length(); i++) {
            Assert.assertEquals(
                    "The label of the updated object at the index " + i
                            + " of the list '" + listId + "' is wrong",
                    itemLabelPrefixForFirstSet + (i + 1),
                    items.getObject(i).getString("label"));
        }
    }

    private void clickToSet0Items_listIsUpdated(String listId,
            String buttonIdFor0Items) {

        WebElement list = findElement(By.id(listId));

        WebElement set0Items = findElement(By.id(buttonIdFor0Items));
        scrollIntoViewAndClick(set0Items);
        waitUntil(driver -> getItems(driver, list).length() == 0);
    }

    public static JsonArray getItems(WebDriver driver, WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].items;", element);
        JsonArray array = Json.createArray();
        if (!(result instanceof List)) {
            return array;
        }
        List<Map<String, ?>> list = (List<Map<String, ?>>) result;
        for (int i = 0; i < list.size(); i++) {
            Map<String, ?> map = list.get(i);
            if (map != null) {
                JsonObject obj = Json.createObject();
                map.entrySet().forEach(entry -> {
                    obj.put(entry.getKey(), String.valueOf(entry.getValue()));
                });
                array.set(i, obj);
            } else {
                array.set(i, Json.createNull());
            }
        }
        return array;
    }

}
