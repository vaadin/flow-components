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
package com.vaadin.flow.component.combobox.testbench;

import java.util.List;

import org.openqa.selenium.By;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasSelectByText;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-combo-box&gt;</code>
 * element.
 */
@Element("vaadin-combo-box")
public class ComboBoxElement extends TestBenchElement
        implements HasLabel, HasSelectByText, HasHelper {

    /**
     * Clears the value of the combobox.
     */
    @Override
    public void clear() {
        setValue(null);
    }

    /**
     * Sets the property "value" as a string.
     * <p>
     *
     * @param value
     *            the value to set
     */
    protected void setValue(String value) {
        setProperty("value", value);
    }

    /**
     * Gets the property "value" as a string.
     * <p>
     *
     * @return the value of the combobox or an empty string if no value is
     *         selected
     */
    protected String getValue() {
        return getPropertyString("value");
    }

    @Override
    public void selectByText(String text) {
        setFilter(text);
        waitForVaadin();
        Boolean success = (Boolean) executeScript("var combobox = arguments[0];" //
                + "var text = arguments[1];" //
                + "var matches = combobox.filteredItems.filter(function(item) {return combobox._getItemLabel(item) == text;});"
                + "if (matches.length == 0) {" //
                + "  return false;" //
                + "} else {" //
                + "  var value = combobox._getItemValue(matches[0]);"
                + "  combobox.value = value;" + "  return true;" //
                + "}", this, text);
        closePopup();
        if (!success) {
            throw new IllegalArgumentException(
                    "Value '" + text + "' not found in the combobox");
        }
    }

    @Override
    public String getSelectedText() {
        return (String) executeScript("var combobox = arguments[0];" //
                + "var selectedItem = combobox.selectedItem;" //
                + "if (!selectedItem) " //
                + "  return '';" //
                + "else " //
                + "  return selectedItem.label;", this);
    }

    public String getInputElementValue() {
        return this.getPropertyString("_inputElementValue");
    }

    /**
     * Opens the popup with options, if it is not already open.
     */
    public void openPopup() {
        callFunction("open");
    }

    /**
     * Close the popup with options, if it is open.
     */
    public void closePopup() {
        callFunction("close");
    }

    /**
     * Checks whether the popup with options is open.
     *
     * @return <code>true</code> if the popup is open, <code>false</code>
     *         otherwiseF
     */
    public boolean isPopupOpen() {
        return getPropertyBoolean("opened");
    }

    /**
     * Gets a list of all available options.
     *
     * @return a list of the options (visible text)
     */
    @SuppressWarnings("unchecked")
    public List<String> getOptions() {
        openPopup();
        return (List<String>) executeScript("var combobox=arguments[0];" //
                + "return combobox.filteredItems.map(function(item) { return combobox._getItemLabel(item);});",
                this);
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
        waitUntil(driver -> !getPropertyBoolean("loading"));
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
     * Gets whether dropdown will open automatically or not.
     *
     * @return @{code true} if enabled, {@code false} otherwise
     */
    public boolean isAutoOpen() {
        return !getPropertyBoolean("autoOpenDisabled");
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        findElement(By.tagName("input")).sendKeys(keysToSend);
    }
}
