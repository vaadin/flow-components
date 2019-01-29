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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.testbench.TestBenchElement;
import elemental.json.JsonObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.openqa.selenium.WebElement;

@Ignore
public class AbstractComboBoxIT extends AbstractComponentIT {

    protected void removeDisabledAttribute(TestBenchElement element) {
        executeScript("arguments[0].removeAttribute('disabled');", element);
    }

    protected void assertItemSelected(String label) {
        Optional<TestBenchElement> itemElement = getItemElements().stream()
                .filter(element -> getItemLabel(element).equals(label))
                .findFirst();
        Assert.assertTrue(
                "Could not find the item with label '" + label
                        + "' which was expected to be selected.",
                itemElement.isPresent());
        Assert.assertEquals(
                "Expected item element with label '" + label
                        + "' to have 'selected' attribute.",
                true, itemElement.get().getProperty("selected"));
    }

    protected String getTextFieldValue(ComboBoxElement comboBox) {
        return (String) executeScript("return arguments[0].inputElement.value",
                comboBox);
    }

    protected void assertLoadedItemsCount(String message, int expectedCount,
            ComboBoxElement comboBox) {
        Assert.assertEquals(message, expectedCount,
                getLoadedItems(comboBox).size());
    }

    // Gets all the loaded json items, but they are not necessarily rendered
    protected List<JsonObject> getLoadedItems(ComboBoxElement comboBox) {
        List<JsonObject> list = (List<JsonObject>) executeScript(
                "return arguments[0].filteredItems.filter("
                        + "item => !(item instanceof Vaadin.ComboBoxPlaceholder));",
                comboBox);
        return list;
    }

    protected void assertRendered(String innerHTML) {
        List<String> overlayContents = getOverlayContents();
        Optional<String> matchingItem = overlayContents.stream()
                .filter(s -> s.equals(innerHTML)).findFirst();
        Assert.assertTrue(
                "Expected to find an item with rendered innerHTML: " + innerHTML
                        + "\nRendered items: "
                        + overlayContents.stream().reduce("",
                                (result, next) -> String.format("%s\n- %s",
                                        result, next)),
                matchingItem.isPresent());
    }

    protected void assertNotRendered(String innerHTML) {
        List<String> overlayContents = getOverlayContents();
        Optional<String> matchingItem = overlayContents.stream()
                .filter(s -> s.equals(innerHTML)).findFirst();
        Assert.assertFalse(
                "Expected to not find an item with rendered innerHTML: "
                        + innerHTML,
                matchingItem.isPresent());
    }

    // Gets the innerHTML of all the actually rendered item elements.
    // There's more items loaded though.
    protected List<String> getOverlayContents() {
        return getItemElements().stream().map(this::getItemLabel)
                .collect(Collectors.toList());
    }

    protected List<String> getNonEmptyOverlayContents() {
        return getOverlayContents().stream()
                .filter(rendered -> !rendered.isEmpty())
                .collect(Collectors.toList());
    }

    protected String getItemLabel(TestBenchElement itemElement) {
        return itemElement.$("div").id("content")
                .getPropertyString("innerHTML");
    }

    protected List<TestBenchElement> getItemElements() {
        return getOverlay().$("div").id("content").$("vaadin-combo-box-item")
                .all().stream()
                .filter(element -> !element.hasAttribute("hidden"))
                .collect(Collectors.toList());
    }

    protected void scrollToItem(ComboBoxElement comboBox, int index) {
        executeScript("arguments[0].$.overlay._scrollIntoView(arguments[1])",
                comboBox, index);
    }

    protected TestBenchElement getOverlay() {
        return $("vaadin-combo-box-overlay").first();
    }

    protected void clickButton(String id) {
        $("button").id(id).click();
    }

    protected List<?> getItems(WebElement combo) {
        executeScript("arguments[0].opened=true", combo);
        List<?> items = (List<?>) getCommandExecutor()
                .executeScript("return arguments[0].filteredItems;", combo);
        executeScript("arguments[0].opened=false", combo);
        return items;
    }

    protected void assertItem(List<?> items, int index, String caption) {
        Map<?, ?> map = (Map<?, ?>) items.get(index);
        Assert.assertEquals(caption, map.get("label"));
    }

    protected String getItemLabel(List<?> items, int index) {
        Map<?, ?> map = (Map<?, ?>) items.get(index);
        return (String) map.get("label");
    }

    protected String getSelectedItemLabel(WebElement combo) {
        return String.valueOf(executeScript(
                "return arguments[0].selectedItem ? arguments[0].selectedItem.label : \"\"",
                combo));
    }

    /**
     * Wait for the items of the specified combobox to fulfill the specified
     * condition.
     *
     * @param combo
     *            The combobox element.
     * @param condition
     *            The condition to wait for.
     */
    protected void waitForItems(WebElement combo,
            Function<List<?>, Boolean> condition) {

        waitUntil(driver -> {
            List comboItems = (List<?>) getCommandExecutor()
                    .executeScript("return arguments[0].filteredItems;", combo);

            return condition.apply(comboItems);
        });
    }
}
