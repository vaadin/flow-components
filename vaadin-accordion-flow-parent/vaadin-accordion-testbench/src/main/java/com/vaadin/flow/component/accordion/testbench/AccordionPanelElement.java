package com.vaadin.flow.component.accordion.testbench;

/*
 * #%L
 * Vaadin Accordion Testbench API
 * %%
 * Copyright (C) 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

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
