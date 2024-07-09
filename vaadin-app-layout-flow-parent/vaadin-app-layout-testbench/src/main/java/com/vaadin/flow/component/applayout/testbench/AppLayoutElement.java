/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.applayout.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-app-layout")
public class AppLayoutElement extends TestBenchElement {

    @SuppressWarnings("unchecked")
    public TestBenchElement getContent() {
        TestBenchElement contentPlaceholder = $(TestBenchElement.class)
                .attribute("content", "").first();

        return (TestBenchElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0];",
                contentPlaceholder);
    }

    public boolean isDrawerFirst() {
        return Boolean.TRUE.equals(getPropertyBoolean("drawerFirst"));
    }

    public void setDrawerFirst(boolean drawerFirst) {
        setProperty("drawerFirst", drawerFirst);
    }

    public boolean isDrawerOpened() {
        return getPropertyBoolean("drawerOpened");
    }

    public void setDrawerOpened(boolean drawerOpened) {
        setProperty("drawerOpened", drawerOpened);
    }

    public boolean isOverlay() {
        return getPropertyBoolean("overlay");
    }

    public DrawerToggleElement getDrawerToggle() {
        return $(DrawerToggleElement.class).first();
    }

}
