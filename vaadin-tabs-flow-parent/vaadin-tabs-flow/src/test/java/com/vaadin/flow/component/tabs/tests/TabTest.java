/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.tabs.Tab;

/**
 * @author Vaadin Ltd.
 */
class TabTest {

    private Tab tab = new Tab();

    @Test
    void shouldCreateEmptyTabWithDefaultState() throws Exception {

        Assertions.assertEquals("", tab.getLabel(), "Initial label is invalid");
        Assertions.assertEquals(0.0, tab.getFlexGrow(), 0.0,
                "Initial flexGrow is invalid");
    }

    @Test
    void shouldCreateTabWithLabel() throws Exception {
        String label = "A label";

        tab = new Tab(label);

        Assertions.assertEquals(label, tab.getLabel(),
                "Initial label is invalid");
        Assertions.assertEquals(0.0, tab.getFlexGrow(), 0.0,
                "Initial flexGrow is invalid");
    }

    @Test
    void shouldSetFlexGrow() throws Exception {
        tab.setFlexGrow(1);

        Assertions.assertEquals(1.0, tab.getFlexGrow(), 0.0,
                "flexGrow is invalid");
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(tab instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        Assertions.assertTrue(tab instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        tab.setAriaLabel("aria label");

        Assertions.assertTrue(tab.getAriaLabel().isPresent());
        Assertions.assertEquals("aria label", tab.getAriaLabel().get());

        tab.setAriaLabel(null);
        Assertions.assertTrue(tab.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        tab.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(tab.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                tab.getAriaLabelledBy().get());

        tab.setAriaLabelledBy(null);
        Assertions.assertTrue(tab.getAriaLabelledBy().isEmpty());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions
                .assertTrue(HasThemeVariant.class.isAssignableFrom(Tab.class));
    }
}
