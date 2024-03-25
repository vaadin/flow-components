/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.testbench;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-checkbox&gt;</code>
 * element.
 */
@Element("vaadin-checkbox")
public class CheckboxElement extends TestBenchElement
        implements HasLabel, HasHelper {
    /**
     * Checks whether the checkbox is checked.
     *
     * @return <code>true</code> if the checkbox is checked, <code>false</code>
     *         if it is not checked or in indeterminate mode
     */
    public boolean isChecked() {
        return getPropertyBoolean("checked");
    }

    /**
     * Sets whether the checkbox is checked.
     *
     * @param checked
     *            <code>true</code> to check the checkbox, <code>false</code> to
     *            uncheck it
     */
    public void setChecked(boolean checked) {
        setProperty("checked", checked);
    }

    @Override
    public String getLabel() {
        return $("label").first().getPropertyString("textContent");
    }

}
