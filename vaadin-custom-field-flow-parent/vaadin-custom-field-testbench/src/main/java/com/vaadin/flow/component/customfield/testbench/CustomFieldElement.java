/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.customfield.testbench;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-custom-field")
public class CustomFieldElement extends TestBenchElement implements HasHelper {

    public String getLabel() {
        return getPropertyString("label");
    }

    public String getErrorMessage() {
        return getPropertyString("errorMessage");
    }

}
