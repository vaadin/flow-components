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
package com.vaadin.flow.component.select.testbench;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasPlaceholder;
import com.vaadin.testbench.HasSelectByText;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Testbench Element API for vaadin-select.
 */
@Element("vaadin-select")
public class SelectElement extends TestBenchElement
        implements HasSelectByText, HasLabel, HasPlaceholder, HasHelper {

    @Element("vaadin-select-item")
    public static class ItemElement extends TestBenchElement {
        public ItemElement() {
            // needed for creating instances inside TB
        }

        // used to convert in streams
        ItemElement(WebElement item, TestBenchCommandExecutor commandExecutor) {
            super(item, commandExecutor);
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

    public boolean isOpened() {
        return getPropertyBoolean("opened");
    }

    public void selectItemByIndex(int index) {
        openPopup();
        getItems().get(index).click();
    }

    public Stream<ItemElement> getItemsStream() {
        openPopup();
        List<WebElement> elements = getPropertyElement("_overlayElement")
                .findElement(By.tagName("vaadin-select-list-box"))
                .findElements(By.tagName("vaadin-select-item"));
        if (elements.size() == 0) {
            return Stream.<ItemElement> builder().build();
        }
        return elements.stream()
                .map(item -> new ItemElement(item, getCommandExecutor()));
    }

    public List<ItemElement> getItems() {
        return getItemsStream().collect(Collectors.toList());
    }

    @Override
    public void selectByText(String text) {
        getItemsStream()
                .filter(item -> text
                        .equals(item.getPropertyString("textContent").trim()))
                .findFirst().get().click();
    }

    @Override
    public String getSelectedText() {
        return getSelectedItem().getText();
    }

    public ItemElement getSelectedOptionItem() {
        return getItemsStream()
                .filter(element -> element.hasAttribute("selected")).findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        "No item selected from popup"));
    }

    public ItemElement getSelectedItem() {
        TestBenchElement valueElement = $("vaadin-select-value-button").first();
        return valueElement.$(ItemElement.class).first();
    }
}
