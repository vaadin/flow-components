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
        var item = contextMenu.addItem("Item");
        contextMenu.setTooltipText(item, "Item tooltip");

        var tooltip = getTooltipElement();
        Assertions.assertTrue(tooltip.isPresent());
        Assertions.assertEquals("tooltip", tooltip.get().getAttribute("slot"));
    }

    @Test
    void setTooltipText_setsTooltipPropertyOnItem() {
        var item = contextMenu.addItem("Item");
        contextMenu.setTooltipText(item, "Item tooltip");

        Assertions.assertEquals("Item tooltip",
                item.getElement().getProperty("tooltip"));
    }

    @Test
    void setTooltipText_multipleItems_singleTooltipElement() {
        var item1 = contextMenu.addItem("Item 1");
        var item2 = contextMenu.addItem("Item 2");
        contextMenu.setTooltipText(item1, "Tooltip 1");
        contextMenu.setTooltipText(item2, "Tooltip 2");

        Assertions.assertEquals(1, getTooltipElements().count());
    }

    @Test
    void setTooltipText_subMenuItem_setsTooltipProperty() {
        var item = contextMenu.addItem("Item");
        var subItem = item.getSubMenu().addItem("Sub item");

        contextMenu.setTooltipText(subItem, "Sub tooltip");

        Assertions.assertEquals("Sub tooltip",
                subItem.getElement().getProperty("tooltip"));
        Assertions.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    void setTooltipPosition_setsTooltipPositionPropertyOnItem() {
        var item = contextMenu.addItem("Item");
        contextMenu.setTooltipPosition(item, TooltipPosition.END);

        Assertions.assertEquals("end",
                item.getElement().getProperty("tooltipPosition"));
    }

    @Test
    void setTooltipPositionNull_clearsTooltipPositionProperty() {
        var item = contextMenu.addItem("Item");
        contextMenu.setTooltipPosition(item, TooltipPosition.END);
        contextMenu.setTooltipPosition(item, null);

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
