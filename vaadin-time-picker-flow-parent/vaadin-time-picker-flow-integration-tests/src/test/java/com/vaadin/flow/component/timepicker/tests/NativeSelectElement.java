/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.testbench.HasStringValueProperty;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("select")
public class NativeSelectElement extends TestBenchElement
        implements HasStringValueProperty {

    @Override
    public void setValue(String value) {
        setProperty("value", value);
        dispatchEvent("change");
        waitForVaadin();
    }
}
