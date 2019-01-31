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

import java.util.List;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.accordion.testbench.AccordionElement.OPENED_PROPERTY;

/**
 * TestBench element for the vaadin-accordion-panel element
 */
@Element("vaadin-accordion-panel")
public class AccordionPanelElement extends TestBenchElement {

    private static final String SLOT_TAG = "slot";
    private static final String SUMMARY_SLOT = "summary";

    /**
     * Checks if the panel is expanded.
     *
     * @return true if expanded of false if otherwise
     */
    public boolean isExpanded() {
        return hasAttribute(OPENED_PROPERTY);
    }

    /**
     * Expands the panel.
     */
    public void expand() {
        setProperty(OPENED_PROPERTY, true);
    }

    /**
     * Collapses the panel.
     */
    public void collapse() {
        setProperty(OPENED_PROPERTY, (Boolean) null);
    }

    /**
     * Gets the summary text if set as text on the component.
     *
     * @return the summary text
     */
    public String getSummaryText() {
        return $("span").attribute(SLOT_TAG, SUMMARY_SLOT).first().getText();
    }

    /**
     * Gets the summary element.
     *
     * @return the summary element
     */
    public TestBenchElement getSummary() {
        return $(TestBenchElement.class).attribute(SLOT_TAG, SUMMARY_SLOT).first();
    }

    /**
     * Gets the content of the panel.
     *
     * @return the content
     */
    public List<TestBenchElement> getContent() {
        return $(TestBenchElement.class).all().stream()
                .filter(e -> !e.hasAttribute(SLOT_TAG))
                .collect(Collectors.toList());
    }
}
