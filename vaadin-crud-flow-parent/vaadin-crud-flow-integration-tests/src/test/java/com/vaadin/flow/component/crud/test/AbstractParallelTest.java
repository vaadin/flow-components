/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;

public abstract class AbstractParallelTest
        extends com.vaadin.tests.AbstractParallelTest {

    protected String getLastEvent() {
        return $(VerticalLayoutElement.class).last().$("span").last().getText();
    }
}
