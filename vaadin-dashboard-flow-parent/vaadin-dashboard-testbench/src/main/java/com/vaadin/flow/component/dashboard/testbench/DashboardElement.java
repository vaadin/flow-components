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
@Element("vaadin-dashboard")
public class DashboardElement extends TestBenchElement {

    /**
     * Returns the widgets in the dashboard.
     *
     * @return The widgets in the dashboard
     */
    public List<DashboardWidgetElement> getWidgets() {
        return getSorted(DashboardWidgetElement.class);
    }

    /**
     * Returns the sections in the dashboard.
     *
     * @return The sections in the dashboard
     */
    public List<DashboardSectionElement> getSections() {
        return getSorted(DashboardSectionElement.class);
    }

    private <T extends TestBenchElement> List<T> getSorted(Class<T> type) {
        return $(type).all().stream()
                .sorted((e1, e2) -> getSortName(e1).compareTo(getSortName(e2)))
                .toList();
    }

    private String getSortName(TestBenchElement element) {
        var wrapper = element.getPropertyElement("parentElement");
        var slot = wrapper.getAttribute("slot");
        var wrapperParent = wrapper.getPropertyElement("parentElement");

        if ($(DashboardSectionElement.class).all().contains(wrapperParent)) {
            return getSortName(wrapperParent) + "-" + slot;
        }
        return slot;
    }
}
