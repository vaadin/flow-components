/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.ironlist.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.demo.TabbedComponentDemoTest;

import elemental.json.JsonArray;
import elemental.json.JsonType;
import elemental.json.JsonValue;

/**
 * Smoke tests for the demo page.
 * <p>
 * Core feature tests are covered at the {@code com.vaadin.ui.iron.list.it}
 * package.
 *
 * @author Vaadin Ltd.
 */
public class IronListViewIT extends TabbedComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/";
    }

    @Test
    public void stringList() {
        openTabAndCheckForErrors("");
        validateListSize(findElement(By.id("list-of-strings")), 3);
    }

    @Test
    public void stringListWithDataProvider() {
        openTabAndCheckForErrors("");
        validateListSize(
                findElement(By.id("list-of-strings-with-dataprovider")), 1000);
    }

    @Test
    public void chuckNorrisFacts() {
        openTabAndCheckForErrors("using-templates");
        validateListSize(findElement(By.id("chuck-norris-facts")), 1000);
    }

    @Test
    public void peopleListWithDataProvider() {
        openTabAndCheckForErrors("using-templates");
        WebElement list = findElement(
                By.id("list-of-people-with-dataprovider"));
        validateListSize(list, 500);
        validatePlaceholderObject(list);
    }

    @Test
    public void disabledListWithTemplates() throws InterruptedException {
        openTabAndCheckForErrors("using-templates");
        WebElement list = findElement(By.id("disabled-list-with-templates"));

        WebElement removalInfo = findElement(By.id("disabled-removal-result"));
        // self check
        Assert.assertTrue(removalInfo.getText().trim().isEmpty());

        // Check that there are not events
        list.findElement(By.tagName("button")).click();

        // nothing has changed
        Assert.assertTrue(removalInfo.getText().trim().isEmpty());

        JsonArray items = IronListIT.getItems(getDriver(), list);
        List<JsonValue> loadedItems = getLoadedItems(list);
        int loadedItemsSize = loadedItems.size();

        Assert.assertNotEquals("Items should be lazy loaded into the list. "
                + "But loaded items size is the same as the whole size of the list",
                items.length(), loadedItemsSize);

        getCommandExecutor().executeScript(
                "arguments[0].scrollToIndex(arguments[1]); return null;", list,
                loadedItems.size());
        loadedItems = getLoadedItems(list);
        Assert.assertNotEquals("Scroll should load more items", loadedItemsSize,
                loadedItems.size());

        WebElement switchEnabled = findElement(
                By.id("switch-enabled-state-string-list"));
        new Actions(getDriver()).moveToElement(switchEnabled).click().perform();

        getCommandExecutor().executeScript(
                "arguments[0].scrollToIndex(0); return null;", list);
        // Check that there is an event now
        list.findElement(By.tagName("button")).click();
        Assert.assertFalse(
                "Didn't get event on clicked element for enabled list",
                removalInfo.getText().trim().isEmpty());
    }

    @Test
    public void rankedListWithEventHandling() {
        openTabAndCheckForErrors("using-templates");
        validateListSize(findElement(By.id("using-events-with-templates")), 29);
    }

    @Test
    public void peopleListWithComponentRenderer() {
        openTabAndCheckForErrors("using-components");
        WebElement list = findElement(By
                .id("list-of-people-with-dataprovider-and-component-renderer"));
        validateListSize(list, 500);
        validatePlaceholderObject(list);
    }

    @Test
    public void disabledPeopleListWithComponentRenderer() {
        openTabAndCheckForErrors("using-components");

        new Actions(getDriver())
                .moveToElement(findElement(By.id("switch-enabled-people-list")))
                .click().perform();
        WebElement list = findElement(By
                .id("list-of-people-with-dataprovider-and-component-renderer"));

        List<WebElement> content = list
                .findElements(By.cssSelector("vaadin-vertical-layout"));

        waitUntil(driver -> content.get(0).getAttribute("disabled") != null);
        Optional<WebElement> notDisabled = content.stream()
                .filter(item -> item.getAttribute("disabled") == null)
                .findFirst();

        if (notDisabled.isPresent()) {
            Assert.fail("Found not disabled item in the list :"
                    + notDisabled.get().getText());
        }

    }

    private void validateListSize(WebElement list, int expectedSize) {
        JsonArray items = IronListIT.getItems(getDriver(), list);
        Assert.assertEquals(
                "There should be " + expectedSize + " items in the '"
                        + list.getAttribute("id") + "' iron-list",
                expectedSize, items.length());
    }

    private List<JsonValue> getLoadedItems(WebElement list) {
        JsonArray items = IronListIT.getItems(getDriver(), list);
        List<JsonValue> result = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JsonValue value = items.get(i);
            if (!value.getType().equals(JsonType.NULL)) {
                result.add(value);
            }
        }
        return result;
    }

    private void validatePlaceholderObject(WebElement list) {
        Map<String, String> obj = (Map<String, String>) executeScript(
                "return arguments[0].$connector.placeholderItem;", list);
        Assert.assertEquals("The placeholderItem object of the '"
                + list.getAttribute("id")
                + "' iron-list should have the '-----' as firstName property",
                "-----", obj.get("firstName"));
    }

}
