/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.tests;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.dom.Element;

/**
 * Unit tests for MenuBar tooltip.
 *
 * @author Vaadin Ltd.
 */
public class MenuBarTooltipTest {

    private MenuBar menuBar;

    @Before
    public void setup() {
        menuBar = new MenuBar();
    }

    @Test
    public void default_doesNotHaveTooltipElement() {
        Assert.assertFalse(getTooltipElement(menuBar).isPresent());
    }

    @Test
    public void addItemWithTooltip_hasTooltipElement() {
        menuBar.addItem("Item", "Item tooltip");
        Assert.assertTrue(getTooltipElement(menuBar).isPresent());
    }

    @Test
    public void addItemWithTooltip_tooltipHasSlot() {
        menuBar.addItem("Item", "Item tooltip");
        Assert.assertEquals("tooltip",
                getTooltipElement(menuBar).get().getAttribute("slot"));
    }

    @Test
    public void addAnotherItemWithTooltip_hasOneTooltipElement() {
        menuBar.addItem("Item 0", "Item 0 tooltip");
        menuBar.addItem("Item 1", "Item 1 tooltip");
        Assert.assertEquals(1, getTooltipElements(menuBar).count());
    }

    private Optional<Element> getTooltipElement(MenuBar menuBar) {
        return getTooltipElements(menuBar).findFirst();
    }

    private Stream<Element> getTooltipElements(MenuBar menuBar) {
        return menuBar.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

}
