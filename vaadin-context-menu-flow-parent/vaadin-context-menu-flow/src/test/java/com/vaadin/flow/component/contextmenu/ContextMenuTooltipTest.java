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
package com.vaadin.flow.component.contextmenu;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
import com.vaadin.flow.dom.Element;

class ContextMenuTooltipTest {

    private ContextMenu contextMenu;

    @BeforeEach
    void setup() {
        contextMenu = new ContextMenu();
    }

    @Test
    void default_doesNotHaveTooltipElement() {
        Assertions.assertFalse(getTooltipElement().isPresent());
    }

    @Test
    void setTooltipText_addsSlottedTooltipElement() {
        var item = contextMenu.addItem("Item 0");
        item.setTooltipText("Item 0 / Tooltip");

        var tooltip = getTooltipElement();
        Assertions.assertTrue(tooltip.isPresent());
        Assertions.assertEquals("tooltip", tooltip.get().getAttribute("slot"));
    }

    @Test
    void setTooltipText_setsTooltipPropertyOnItem() {
        var item = contextMenu.addItem("Item 0");
        item.setTooltipText("Item 0 / Tooltip");

        Assertions.assertEquals("Item 0 / Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void setTooltipText_multipleItems_singleTooltipElement() {
        var item0 = contextMenu.addItem("Item 0");
        var item1 = contextMenu.addItem("Item 1");
        item0.setTooltipText("Item 0 / Tooltip");
        item1.setTooltipText("Item 1 / Tooltip");

        Assertions.assertEquals(1, getTooltipElements().count());
    }

    @Test
    void setTooltipText_subMenuItem_setsTooltipProperty() {
        var item = contextMenu.addItem("Item 0");
        var subItem = item.getSubMenu().addItem("Item 0-0");

        subItem.setTooltipText("Item 0-0 / Tooltip");

        Assertions.assertEquals("Item 0-0 / Tooltip",
                subItem.getElement().getProperty("tooltip"));
        Assertions.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    void addItemWithTextAndTooltip_setsTooltipProperty() {
        var item = contextMenu.addItem("Item 0", "Item 0 / Tooltip");

        Assertions.assertEquals("Item 0 / Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void addItemWithComponentAndTooltip_setsTooltipProperty() {
        var item = contextMenu.addItem(new Span("Item 0"), "Item 0 / Tooltip");

        Assertions.assertEquals("Item 0 / Tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void subMenu_addItemWithTextAndTooltip_setsTooltipProperty() {
        var item = contextMenu.addItem("Item 0");
        var subItem = item.getSubMenu().addItem("Item 0-0",
                "Item 0-0 / Tooltip");

        Assertions.assertEquals("Item 0-0 / Tooltip",
                subItem.getElement().getProperty("tooltip"));
    }

    @Test
    void subMenu_addItemWithComponentAndTooltip_setsTooltipProperty() {
        var item = contextMenu.addItem("Item 0");
        var subItem = item.getSubMenu().addItem(new Span("Item 0-0"),
                "Item 0-0 / Tooltip");

        Assertions.assertEquals("Item 0-0 / Tooltip",
                subItem.getElement().getProperty("tooltip"));
    }

    @Test
    void subMenu_addItemWithTextTooltipAndListener_setsTooltipProperty() {
        var item = contextMenu.addItem("Item 0");
        var subItem = item.getSubMenu().addItem("Item 0-0",
                "Item 0-0 / Tooltip", e -> {
                });

        Assertions.assertEquals("Item 0-0 / Tooltip",
                subItem.getElement().getProperty("tooltip"));
    }

    @Test
    void subMenu_addItemWithComponentTooltipAndListener_setsTooltipProperty() {
        var item = contextMenu.addItem("Item 0");
        var subItem = item.getSubMenu().addItem(new Span("Item 0-0"),
                "Item 0-0 / Tooltip", e -> {
                });

        Assertions.assertEquals("Item 0-0 / Tooltip",
                subItem.getElement().getProperty("tooltip"));
    }

    @Test
    void setTooltipPosition_setsTooltipPositionPropertyOnItem() {
        var item = contextMenu.addItem("Item 0");
        item.setTooltipPosition(TooltipPosition.END);

        Assertions.assertEquals("end",
                item.getElement().getProperty("tooltipPosition"));
    }

    @Test
    void setTooltipPositionNull_clearsTooltipPositionProperty() {
        var item = contextMenu.addItem("Item 0");
        item.setTooltipPosition(TooltipPosition.END);
        item.setTooltipPosition(null);

        Assertions.assertNull(item.getElement().getProperty("tooltipPosition"));
    }

    private Optional<Element> getTooltipElement() {
        return getTooltipElements().findFirst();
    }

    private Stream<Element> getTooltipElements() {
        return contextMenu.getElement().getChildren()
                .filter(child -> "vaadin-tooltip".equals(child.getTag()));
    }
}
