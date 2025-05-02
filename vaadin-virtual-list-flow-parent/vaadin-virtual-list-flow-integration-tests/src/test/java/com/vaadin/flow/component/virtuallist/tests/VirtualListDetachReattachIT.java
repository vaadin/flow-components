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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

@TestPath("vaadin-virtual-list/detach-reattach")
public class VirtualListDetachReattachIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        WebElement loadingIndicator = findElement(
                By.className("v-loading-indicator"));
        waitUntil(driver -> !loadingIndicator.isDisplayed()
                && findElements(By.tagName("vaadin-virtual-list")).size() > 0);
    }

    @Test
    public void changeContainers_itemsAreStillShown() {
        WebElement container1 = findElement(By.id("container-1"));
        WebElement container2 = findElement(By.id("container-2"));
        WebElement attach1 = findElement(By.id("list-attach-1"));
        WebElement attach2 = findElement(By.id("list-attach-2"));

        WebElement list = container1.findElement(By.id("list"));
        assertItemsArePresent(list, 20);

        // sets a property on the $connector, to validate that the connector
        // is not reset when changing containers
        executeScript("arguments[0].$connector._isUsingTheSameInstance = true",
                list);

        attach2.click();
        list = container2.findElement(By.id("list"));
        assertItemsArePresent(list, 20);
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        list));

        attach1.click();
        list = container1.findElement(By.id("list"));
        assertItemsArePresent(list, 20);
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        list));
    }

    @Test
    public void detachAndReattach_itemsAreStillShown() {
        WebElement container1 = findElement(By.id("container-1"));
        WebElement attach1 = findElement(By.id("list-attach-1"));
        WebElement detach = findElement(By.id("list-detach"));

        WebElement list = container1.findElement(By.id("list"));

        assertItemsArePresent(list, 20);

        detach.click();
        waitForElementNotPresent(By.id("list"));
        attach1.click();
        list = container1.findElement(By.id("list"));
        assertItemsArePresent(list, 20);
    }

    @Test
    public void setInvisibleAndVisible_itemsAreStillShown() {
        WebElement container1 = findElement(By.id("container-1"));
        WebElement invisible = findElement(By.id("list-invisible"));
        WebElement visible = findElement(By.id("list-visible"));

        WebElement list = container1.findElement(By.id("list"));

        assertItemsArePresent(list, 20);
        // sets a property on the $connector, to validate that the connector
        // is not reset when changing visibility
        executeScript("arguments[0].$connector._isUsingTheSameInstance = true",
                list);

        invisible.click();
        waitUntil(driver -> "true".equals(list.getDomAttribute("hidden")));
        visible.click();
        waitUntil(driver -> list.getDomAttribute("hidden") == null);
        assertItemsArePresent(list, 20);
        Assert.assertTrue("The $connector instance should be preserved",
                (Boolean) executeScript(
                        "return arguments[0].$connector._isUsingTheSameInstance",
                        list));
    }

    @Test
    public void useComponentRenderer_detachAndReattachInSameRoundtrip_itemsAreStillShown() {
        WebElement container1 = findElement(By.id("container-1"));
        findElement(By.id("list-use-component-renderer")).click();

        WebElement list = container1.findElement(By.id("list"));

        List<WebElement> items = list
                .findElements(By.className("component-rendered"));
        Assert.assertEquals(20, items.size());

        findElement(By.id("list-detach-and-reattach")).click();

        list = container1.findElement(By.id("list"));
        items = list.findElements(By.className("component-rendered"));
        Assert.assertEquals(20, items.size());
    }

    private void assertItemsArePresent(WebElement list, int length) {
        JsonArray items = getItems(driver, list);
        Assert.assertEquals(length, items.length());
        for (int i = 0; i < items.length(); i++) {
            JsonObject obj = items.getObject(i);
            Assert.assertEquals("Person " + (i + 1),
                    getPropertyString(obj, "label"));
        }
    }

    private String getPropertyString(JsonObject json, String propertyName) {
        var keyForProperty = Arrays.stream(json.keys())
                .filter(key -> key.endsWith(propertyName)).findFirst().get();
        return json.getString(keyForProperty);
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
