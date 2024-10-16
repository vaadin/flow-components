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
        return $(DashboardWidgetElement.class).all();
    }

    /**
     * Returns the sections in the dashboard.
     *
     * @return The sections in the dashboard
     */
    public List<DashboardSectionElement> getSections() {
        return $(DashboardSectionElement.class).all();
    }
}
