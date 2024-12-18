/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.testbench;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * @author Vaadin Ltd
 */
@Element("vaadin-dashboard-section")
public class DashboardSectionElement extends TestBenchElement {

    /**
     * Returns the title of the section.
     *
     * @return the {@code sectionTitle} property from the web component
     */
    public String getTitle() {
        return getPropertyString("sectionTitle");
    }

    /**
     * Returns the widgets in the section.
     *
     * @return The widgets in the section
     */
    public List<DashboardWidgetElement> getWidgets() {
        return $(DashboardWidgetElement.class).all().stream()
                .sorted((w1, w2) -> getSortIndex(w1).compareTo(getSortIndex(w2)))
                .toList();
    }

    private Float getSortIndex(DashboardWidgetElement widget) {
        var slotName = widget.getPropertyString("parentElement", "slot");
        return Float.parseFloat(slotName.split("-")[1]);
    }
}
