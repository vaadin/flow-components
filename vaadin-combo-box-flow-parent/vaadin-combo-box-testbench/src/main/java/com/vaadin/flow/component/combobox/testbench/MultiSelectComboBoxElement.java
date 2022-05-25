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

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.By;

import java.util.List;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-multi-select-combo-box&gt;</code> element.
 */
@Element("vaadin-multi-select-combo-box")
public class MultiSelectComboBoxElement extends TestBenchElement
        implements HasLabel, HasHelper {

    public String getInputElementValue() {
        return this.getPropertyString("_inputElementValue");
    }

    /**
     * Opens the popup with options, if it is not already open.
     */
    public void openPopup() {
        setProperty("opened", true);
    }

    /**
     * Close the popup with options, if it is open.
     */
    public void closePopup() {
        setProperty("opened", false);
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
        return (List<String>) executeScript("const comboBox=arguments[0];"
                + "return comboBox.filteredItems.map(function(item) { return comboBox._getItemLabel(item);});",
                getInternalComboBox());
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
     * @return @{code true} if enabled, {@code false} otherwise
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
