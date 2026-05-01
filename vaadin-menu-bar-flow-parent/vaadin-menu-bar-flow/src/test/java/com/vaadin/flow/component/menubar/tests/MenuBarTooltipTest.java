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
package com.vaadin.flow.component.menubar.tests;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
import com.vaadin.flow.dom.Element;

/**
 * Unit tests for MenuBar tooltip.
 *
 * @author Vaadin Ltd.
 */
class MenuBarTooltipTest {

    private MenuBar menuBar;

    @BeforeEach
    void setup() {
        menuBar = new MenuBar();
    }

    @Test
    void default_doesNotHaveTooltipElement() {
        Assertions.assertFalse(getTooltipElement(menuBar).isPresent());
    }

    @Test
    void addItemWithTooltip_hasTooltipElement() {
        menuBar.addItem("Item 0", "Item 0 / Tooltip");
        Assertions.assertTrue(getTooltipElement(menuBar).isPresent());
    }

    @Test
    void addItemWithTooltip_tooltipHasSlot() {
        menuBar.addItem("Item 0", "Item 0 / Tooltip");
        Assertions.assertEquals("tooltip",
                getTooltipElement(menuBar).get().getAttribute("slot"));
    }

    @Test
    void addAnotherItemWithTooltip_hasOneTooltipElement() {
        menuBar.addItem("Item 0", "Item 0 / Tooltip");
        menuBar.addItem("Item 1", "Item 1 / Tooltip");
        Assertions.assertEquals(1, getTooltipElements(menuBar).count());
    }

    @Test
    void addItemWithTextAndTooltip_setsTooltipProperty() {
        var item = menuBar.addItem("Item 0", "Item 0 / Tooltip");
        Assertions.assertEquals("Item 0 / Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void addItemWithComponentAndTooltip_setsTooltipProperty() {
        var item = menuBar.addItem(new Span("Item 0"), "Item 0 / Tooltip");
        Assertions.assertEquals("Item 0 / Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void addItemWithTextTooltipAndListener_setsTooltipProperty() {
        var item = menuBar.addItem("Item 0", "Item 0 / Tooltip", e -> {
        });
        Assertions.assertEquals("Item 0 / Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void setTooltipText_updatesTooltipProperty() {
        var item = menuBar.addItem("Item 0", "Item 0 / Tooltip");
        item.setTooltipText("Item 0 / Updated Tooltip");

        Assertions.assertEquals("Item 0 / Updated Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void setTooltipText_subMenuItem_setsTooltipProperty() {
        var rootItem = menuBar.addItem("Item 0");
        var subItem = rootItem.getSubMenu().addItem("Item 0-0");

        subItem.setTooltipText("Item 0-0 / Tooltip");

        Assertions.assertEquals("Item 0-0 / Tooltip",
                subItem.getElement().getProperty("tooltip"));
        Assertions.assertTrue(getTooltipElement(menuBar).isPresent());
    }

    @Test
    void setTooltipPosition_setsTooltipPositionPropertyOnItem() {
        var item = menuBar.addItem("Item 0");
        item.setTooltipPosition(TooltipPosition.BOTTOM);

        Assertions.assertEquals("bottom",
                item.getElement().getProperty("tooltipPosition"));
    }

    @Test
    void setTooltipPositionNull_clearsTooltipPositionProperty() {
        var item = menuBar.addItem("Item 0");
        item.setTooltipPosition(TooltipPosition.BOTTOM);
        item.setTooltipPosition(null);

        Assertions.assertNull(item.getElement().getProperty("tooltipPosition"));
    }

    private Optional<Element> getTooltipElement(MenuBar menuBar) {
        return getTooltipElements(menuBar).findFirst();
    }

    private Stream<Element> getTooltipElements(MenuBar menuBar) {
        return menuBar.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

}
