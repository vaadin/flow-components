/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.accordion.testbench;

import com.vaadin.testbench.elementsbase.Element;
import com.vaadin.flow.component.details.testbench.DetailsElement;

import static com.vaadin.flow.component.accordion.testbench.AccordionElement.OPENED_PROPERTY;

/**
 * TestBench element for the vaadin-accordion-panel element
 */
@Element("vaadin-accordion-panel")
public class AccordionPanelElement extends DetailsElement {

    /**
     * Opens the panel.
     */
    public void open() {
        setProperty(OPENED_PROPERTY, true);
    }

    /**
     * Closes the panel.
     */
    public void close() {
        setProperty(OPENED_PROPERTY, (Boolean) null);
    }
}
