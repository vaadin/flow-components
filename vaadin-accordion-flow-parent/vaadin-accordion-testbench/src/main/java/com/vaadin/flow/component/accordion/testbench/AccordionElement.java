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

import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * TestBench element for the vaadin-accordion element
 */
@Element("vaadin-accordion")
public class AccordionElement extends TestBenchElement {

    static final String OPENED_PROPERTY = "opened";

    /**
     * Closes the opened panel (if any) in the accordion.
     */
    public void close() {
        setProperty(OPENED_PROPERTY, (Boolean) null);
    }

    /**
     * Opens the panel at the specified index. The first panel is at index zero.
     *
     * @param index the index of the panel to be opened
     */
    public void open(int index) {
        setProperty(OPENED_PROPERTY, index);
    }

    /**
     * Gets the index of the opened panel or null if the accordion is closed.
     *
     * @return the index of the opened panel or null if closed.
     */
    public OptionalInt getOpenedIndex() {
        final String openedAttribute = getAttribute(OPENED_PROPERTY);
        return openedAttribute == null ?
                OptionalInt.empty() :
                OptionalInt.of(Integer.valueOf(openedAttribute));
    }

    /**
     * Gets the the opened panel or null if the accordion is closed.
     *
     * @return the opened panel or null if closed.
     */
    public Optional<AccordionPanelElement> getOpenedPanel() {
        final ElementQuery<AccordionPanelElement> openedPanels
                = $(AccordionPanelElement.class).attribute(OPENED_PROPERTY, "");

        return !openedPanels.exists() ? Optional.empty() : Optional.of(openedPanels.first());
    }
}
