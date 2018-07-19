package com.vaadin.flow.component.applayout.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-tab")
public class MenuItemElement extends TestBenchElement {

    public String getTitle() {
        return getAttribute("title");
    }

    public TestBenchElement getIcon() {
        return $(TestBenchElement.class).attribute("role", "img").first();
    }
}
