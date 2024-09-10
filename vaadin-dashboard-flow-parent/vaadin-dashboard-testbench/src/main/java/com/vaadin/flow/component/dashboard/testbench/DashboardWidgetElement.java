/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * @author Vaadin Ltd
 */
@Element("vaadin-dashboard-widget")
public class DashboardWidgetElement extends TestBenchElement {

    /**
     * Returns the title of the widget.
     *
     * @return the {@code widgetTitle} property from the web component
     */
    public String getTitle() {
        return getPropertyString("widgetTitle");
    }

    /**
     * Returns the colspan of the widget.
     *
     * @return the {@code --vaadin-dashboard-item-colspan} computed style from
     *         the web component
     */
    public Integer getColspan() {
        var colspanStr = getComputedCssValue("--vaadin-dashboard-item-colspan");
        return colspanStr.isEmpty() ? null : Integer.valueOf(colspanStr);
    }

    /**
     * Returns the rowspan of the widget.
     *
     * @return the {@code --vaadin-dashboard-item-rowspan} computed style from
     *         the web component
     */
    public Integer getRowspan() {
        var rowspanStr = getComputedCssValue("--vaadin-dashboard-item-rowspan");
        return rowspanStr.isEmpty() ? null : Integer.valueOf(rowspanStr);
    }

    /**
     * Returns the content of the widget.
     *
     * @return the content element set to the widget
     */
    public TestBenchElement getContent() {
        Object content = executeScript(
                "return Array.from(arguments[0].children).filter(child => !child.slot)[0]",
                this);
        return content == null ? null : (TestBenchElement) content;
    }

    /**
     * Returns the header of the widget.
     *
     * @return the header element set to the widget
     */
    public TestBenchElement getHeader() {
        Object header = executeScript(
                "return Array.from(arguments[0].children).filter(child => child.slot === 'header')[0]",
                this);
        return header == null ? null : (TestBenchElement) header;
    }

    private String getComputedCssValue(String propertyName) {
        return (String) executeScript(
                "return getComputedStyle(arguments[0]).getPropertyValue(arguments[1]);",
                this, propertyName);
    }
}
