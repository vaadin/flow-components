/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.select.testbench;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasPlaceholder;
import com.vaadin.testbench.HasSelectByText;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import com.vaadin.testbench.elementsbase.Element;

/**
 * Testbench Element API for vaadin-select.
 */
@Element("vaadin-select")
public class SelectElement extends TestBenchElement implements HasSelectByText,
        HasLabel, HasPlaceholder, HasHelper, HasValidation {

    @Element("vaadin-select-item")
    public static class ItemElement extends TestBenchElement {
        public ItemElement() {
            // needed for creating instances inside TB
        }

        // used to convert in streams
        ItemElement(WebElement item, TestBenchCommandExecutor commandExecutor) {
            super(item, commandExecutor);
        }

        /**
         * Gets the text content of the item. The text is available while the
         * popup is closed.
         *
         * @return the text content of the item, trimmed
         */
        @Override
        public String getText() {
            // Items are in the DOM while the popup is closed, but the default
            // implementation returns only visible text
            return getPropertyString("textContent").trim();
        }
    }

    /**
     * Opens the popup with options, if it is not already open.
     */
    public void openPopup() {
        setProperty("opened", true);
    }

    /**
     * Closes the popup with options, if it is open.
     */
    public void closePopup() {
        setProperty("opened", false);
    }

    /**
     * Checks whether the popup with options is open.
     *
     * @return {@code true} if the popup is open, {@code false} otherwise
     */
    public boolean isOpened() {
        return getPropertyBoolean("opened");
    }

    /**
     * Selects the item with the given index by clicking it in the popup. Opens
     * the popup if it is not open. Clicking the item closes the popup.
     *
     * @param index
     *            the index of the item to select
     */
    public void selectItemByIndex(int index) {
        openPopup();
        getItems().get(index).click();
    }

    /**
     * Gets the items in the popup as a stream. Does not open the popup.
     *
     * @return a stream of the items in the popup
     */
    public Stream<ItemElement> getItemsStream() {
        List<WebElement> elements = getPropertyElement("_menuElement")
                .findElements(By.tagName("vaadin-select-item"));
        if (elements.size() == 0) {
            return Stream.<ItemElement> builder().build();
        }
        return elements.stream()
                .map(item -> new ItemElement(item, getCommandExecutor()));
    }

    /**
     * Gets the items in the popup. Does not open the popup.
     *
     * @return the items in the popup
     */
    public List<ItemElement> getItems() {
        return getItemsStream().toList();
    }

    /**
     * Selects the item with the given text by clicking it in the popup. Opens
     * the popup if it is not open. Clicking the item closes the popup.
     *
     * @param text
     *            the text of the item to select
     * @throws NoSuchElementException
     *             if no item has the given text
     */
    @Override
    public void selectByText(String text) {
        openPopup();
        getItemsStream().filter(item -> text.equals(item.getText())).findFirst()
                .get().click();
    }

    @Override
    public String getSelectedText() {
        ItemElement selectedItem = getSelectedItem();
        return selectedItem == null ? "" : selectedItem.getText();
    }

    /**
     * Gets the selected item in the popup. Does not open the popup.
     *
     * @return the selected item in the popup
     * @throws NoSuchElementException
     *             if no item is selected
     */
    public ItemElement getSelectedOptionItem() {
        return getItemsStream()
                .filter(element -> element.hasAttribute("selected")).findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        "No item selected from popup"));
    }

    /**
     * Gets the currently selected item shown in the value button, or
     * {@code null} if nothing is selected.
     *
     * @return the selected item, or {@code null} if nothing is selected
     */
    public ItemElement getSelectedItem() {
        TestBenchElement valueElement = $("vaadin-select-value-button").first();
        return valueElement.$(ItemElement.class).all().stream().findFirst()
                .orElse(null);
    }
}
