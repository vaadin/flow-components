package com.vaadin.flow.component.applayout.testbench;

/*
 * #%L
 * Vaadin App Layout Testbench API
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

@Element("vaadin-app-layout")
public class AppLayoutElement extends TestBenchElement {

    public TestBenchElement getBranding() {
        return $(TestBenchElement.class).attribute("slot", "branding").first();
    }

    public TestBenchElement getContent() {
        TestBenchElement contentPlaceholder = $(TestBenchElement.class).attribute("part", "content").first();

        return (TestBenchElement) executeScript("return arguments[0].firstElementChild.assignedNodes()[0];",
                contentPlaceholder);
    }

    public <T extends TestBenchElement> T getMenu(Class<T> clazz) {
        return $(clazz).attribute("slot", "menu").waitForFirst();
    }

    public AppLayoutMenuElement getAppLayoutMenuElement() {
        return getMenu(AppLayoutMenuElement.class);
    }

}
