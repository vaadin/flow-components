package com.vaadin.flow.component.accordion.testbench;

/*
 * #%L
 * Vaadin Accordion Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the vaadin-accordion element
 */
@Element("vaadin-accordion")
public class AccordionElement extends TestBenchElement {

    static final String OPENED_PROPERTY = "opened";

    public void collapse() {
        setProperty(OPENED_PROPERTY, (Boolean) null);
    }

    public void expand(int index) {
        setProperty(OPENED_PROPERTY, index);
    }

    public Integer getExpandedIndex() {
        final String openedAttribute = getAttribute(OPENED_PROPERTY);
        return openedAttribute == null ? null : Integer.valueOf(openedAttribute);
    }

    public AccordionPanelElement getExpandedPanel() {
        return !$(AccordionPanelElement.class).attribute(OPENED_PROPERTY, "").exists() ? null
                : $(AccordionPanelElement.class).attribute(OPENED_PROPERTY, "").first();
    }
}
