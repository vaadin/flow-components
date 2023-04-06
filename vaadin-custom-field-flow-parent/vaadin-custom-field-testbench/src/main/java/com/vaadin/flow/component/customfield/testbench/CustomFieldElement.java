/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.customfield.testbench;

import com.vaadin.testbench.ElementQuery;

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

    /**
     * {@inheritDoc}
     */
    // TODO: Remove once https://github.com/vaadin/testbench/issues/1299 is
    // fixed
    @Override
    public TestBenchElement getHelperComponent() {
        final ElementQuery<TestBenchElement> query = $(TestBenchElement.class)
                .attribute("slot", "helper");
        if (query.exists()) {
            TestBenchElement last = query.last();
            // To avoid getting the "slot" element, for components with slotted
            // slots
            if (!"slot".equals(last.getTagName())
                    && this.equals(last.getPropertyElement("parentElement"))) {
                return last;
            }
        }
        return null;
    }
}
