package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;

public abstract class AbstractParallelTest
        extends com.vaadin.tests.AbstractParallelTest {
    protected String getLastEvent() {
        return $(VerticalLayoutElement.class).last().$("span").last().getText();
    }
}
