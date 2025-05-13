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
package com.vaadin.flow.component.combobox.testbench;

import java.util.List;

import org.openqa.selenium.By;

import com.vaadin.testbench.HasClearButton;
import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasPlaceholder;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-multi-select-combo-box&gt;</code> element.
 */
@Element("vaadin-multi-select-combo-box")
public class MultiSelectComboBoxElement extends TestBenchElement implements
        HasLabel, HasPlaceholder, HasHelper, HasClearButton, HasValidation {

    public String getInputElementValue() {
        return this.getPropertyString("_inputElementValue");
    }

    /**
     * Opens the popup, if it is not already open.
     */
    public void openPopup() {
        setProperty("opened", true);
    }

    /**
     * Close the popup, if it is open.
     */
    public void closePopup() {
        setProperty("opened", false);
    }

    /**
     * Checks whether the popup is open.
     *
     * @return <code>true</code> if the popup is open, <code>false</code>
     *         otherwise
     */
    public boolean isPopupOpen() {
        return getPropertyBoolean("opened");
    }

    /**
     * Opens the popup, and gets the labels of the items that are currently
     * loaded in the popup
     *
     * @return labels of the items that are loaded in the popup
     */
    @SuppressWarnings("unchecked")
    public List<String> getOptions() {
        openPopup();
        waitForLoadingFinished();
        //@formatter:off
        String script =
                "const comboBox=arguments[0];" +
                "return comboBox.$.comboBox.filteredItems.map(item => item.label || '')";
        //@formatter:on
        return (List<String>) executeScript(script, this);
    }

    /**
     * Attempts to select an item from the popup by matching the label. Throws
     * an {@link IllegalArgumentException} if the popup does not contain an item
     * with the specified label. Does nothing if the item is already selected.
     *
     * @param label
     *            The label of the item to select from the popup
     * @throws IllegalArgumentException
     *             if there is no item with the specified label
     */
    public void selectByText(String label) {
        setFilter(label);
        //@formatter:off
        String script =
                "const combobox = arguments[0];" +
                "const label = arguments[1];" +
                "const itemToSelect = combobox.$.comboBox.filteredItems.find(item => item.label === label);" +
                "if (!itemToSelect) return false;" +
                "const isSelected = combobox.selectedItems.some(item => item.key === itemToSelect.key);" +
                "if (!isSelected) {" +
                "  combobox.selectedItems = [...combobox.selectedItems, itemToSelect];" +
                "}" +
                "return true;";
        //@formatter:on
        Boolean success = (Boolean) executeScript(script, this, label);
        closePopup();
        if (!success) {
            throw new IllegalArgumentException("Item with label '" + label
                    + "' not found in the combobox");
        }
    }

    /**
     * Attempts to deselect an item that is currently selected, by matching the
     * label. Does nothing if the item is not selected.
     *
     * @param label
     *            The label of the item to deselect
     */
    public void deselectByText(String label) {
        //@formatter:off
        String script =
                "const combobox = arguments[0];" +
                "const label = arguments[1];" +
                "const isSelected = combobox.selectedItems.some(item => item.label === label);" +
                "if (isSelected) {" +
                "  combobox.selectedItems = combobox.selectedItems.filter(item => item.label !== label);" +
                "}";
        //@formatter:on
        executeScript(script, this, label);
    }

    /**
     * Deselects all items, effectively clearing the value.
     */
    public void deselectAll() {
        String script = "const combobox = arguments[0]; combobox.selectedItems = [];";
        executeScript(script, this);
    }

    /**
     * Gets the labels of the currently selected items.
     *
     * @return the labels of the currently selected items
     */
    @SuppressWarnings("unchecked")
    public List<String> getSelectedTexts() {
        //@formatter:off
        String script =
                "const combobox = arguments[0];" +
                "return combobox.selectedItems.map(item => item.label)";
        //@formatter:on
        return (List<String>) executeScript(script, this);
    }

    /**
     * Sets the filter for the options in the popup.
     *
     * @param filter
     *            the filter to use for filtering options
     */
    public void setFilter(String filter) {
        openPopup();
        setProperty("filter", filter);
        waitForLoadingFinished();
    }

    /**
     * Gets the filter for the options in the popup.
     *
     * @return the filter to use for filtering options
     */
    public String getFilter() {
        return getPropertyString("filter");
    }

    /**
     * Waits until the combo box has finished loading items to show in the popup
     */
    public void waitForLoadingFinished() {
        waitUntil(
                driver -> !getInternalComboBox().getPropertyBoolean("loading"));
    }

    /**
     * Gets whether dropdown will open automatically or not.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isAutoOpen() {
        return !getPropertyBoolean("autoOpenDisabled");
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        findElement(By.tagName("input")).sendKeys(keysToSend);
    }

    private TestBenchElement getInternalComboBox() {
        return $("vaadin-multi-select-combo-box-internal").first();
    }
}
