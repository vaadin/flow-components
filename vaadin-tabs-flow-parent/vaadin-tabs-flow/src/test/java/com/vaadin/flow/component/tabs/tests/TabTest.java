/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.tabs.tests;

import org.junit.Test;

import com.vaadin.flow.component.tabs.Tab;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Vaadin Ltd.
 */
public class TabTest {

    private Tab tab = new Tab();

    @Test
    public void shouldCreateEmptyTabWithDefaultState() throws Exception {

        assertThat("Initial label is invalid", tab.getLabel(), is(""));
        assertThat("Initial flexGrow is invalid", tab.getFlexGrow(), is(0.0));
    }

    @Test
    public void shouldCreateTabWithLabel() throws Exception {
        String label = "A label";

        tab = new Tab(label);

        assertThat("Initial label is invalid", tab.getLabel(), is(label));
        assertThat("Initial flexGrow is invalid", tab.getFlexGrow(), is(0.0));
    }

    @Test
    public void shouldSetFlexGrow() throws Exception {
        tab.setFlexGrow(1);

        assertThat("flexGrow is invalid", tab.getFlexGrow(), is(1.0));
    }
}
