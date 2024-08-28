/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.testbench;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;

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
     * @return the {@code --vaadin-dashboard-item-colspan} style from the
     *         wrapper of the web component
     */
    public Integer getColspan() {
        TestBenchElement wrapper = getWrapper();
        if (wrapper == null) {
            return null;
        }
        String colspanStr = getStyle(wrapper,
                "--vaadin-dashboard-item-colspan");
        if (colspanStr == null) {
            return null;
        }
        return Integer.valueOf(colspanStr);
    }

    private TestBenchElement getWrapper() {
        return findElement(By.xpath(".."));
    }

    private static String getStyle(TestBenchElement element, String name) {
        String style = element.getAttribute("style");
        if (style == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(name + ": (.*?);");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
