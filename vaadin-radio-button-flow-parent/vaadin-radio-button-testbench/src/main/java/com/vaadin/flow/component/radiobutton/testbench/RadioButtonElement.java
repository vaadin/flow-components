/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-radio-button&gt;</code>
 * element.
 */
@Element("vaadin-radio-button")
public class RadioButtonElement extends TestBenchElement {

    /**
     * Gets the item (visible text) of the radio button.
     *
     * @return the text of the radio button
     */
    public String getItem() {
        return getPropertyString("firstChild", "textContent");
    }

    /**
     * Gets the value of the radio button.
     *
     * @return the value
     */
    public String getValue() {
        return getPropertyString("value");
    }

    /**
     * Sets whether the radio button is checked.
     *
     * @param checked
     *            <code>true</code> to check the radio button,
     *            <code>false</code> to uncheck it
     */
    public void setChecked(boolean checked) {
        setProperty("checked", checked);
    }
}
