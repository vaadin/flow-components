/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.tabs.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.tabs.Tab;

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

    @Test
    public void implementsHasTooltip() {
        assertTrue(tab instanceof HasTooltip);
    }

    @Test
    public void implementHasAriaLabel() {
        assertTrue(tab instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        tab.setAriaLabel("aria label");

        Assert.assertTrue(tab.getAriaLabel().isPresent());
        Assert.assertEquals("aria label", tab.getAriaLabel().get());

        tab.setAriaLabel(null);
        Assert.assertTrue(tab.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        tab.setAriaLabelledBy("aria-labelledby");

        Assert.assertTrue(tab.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", tab.getAriaLabelledBy().get());

        tab.setAriaLabelledBy(null);
        Assert.assertTrue(tab.getAriaLabelledBy().isEmpty());
    }
}
