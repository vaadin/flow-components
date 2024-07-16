/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.tabs.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.tabs.Tab;

public class HasLabelTest {

    @Test
    public void tab() {
        Tab t = new Tab();
        Assert.assertTrue(t instanceof HasLabel);
    }

}
