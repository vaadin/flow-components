/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.component.tabs.tests;

import com.vaadin.flow.component.HasAriaLabel;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.shared.HasTooltip;
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
