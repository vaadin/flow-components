/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * @author Vaadin Ltd
 */
@Tag("vaadin-dashboard-widget")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha8")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("@vaadin/dashboard/src/vaadin-dashboard-widget.js")
// @NpmPackage(value = "@vaadin/dashboard", version = "24.6.0-alpha0")
public class DashboardWidget extends Component {

    /**
     * Returns the title of the widget.
     *
     * @return the {@code widget-title} property from the web component
     */
    public String getTitle() {
        return getElement().getProperty("widgetTitle");
    }

    /**
     * Sets the title of the widget.
     *
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        getElement().setProperty("widgetTitle", title == null ? "" : title);
    }
}
