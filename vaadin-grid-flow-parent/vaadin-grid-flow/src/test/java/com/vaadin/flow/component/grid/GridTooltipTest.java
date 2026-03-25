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
package com.vaadin.flow.component.grid;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Element;

/**
 * Unit tests for Grid tooltip.
 *
 * @author Vaadin Ltd.
 */
class GridTooltipTest {

    private Grid<String> grid;

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        grid.addColumn(item -> item);
    }

    @Test
    void default_doesNotHaveTooltipElement() {
        Assertions.assertFalse(getTooltipElement(grid).isPresent());
    }

    @Test
    void setColumnTooltipGenerator_hasTooltipElement() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assertions.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    void setGridTooltipGenerator_hasTooltipElement() {
        grid.setTooltipGenerator(item -> item);
        Assertions.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    void setColumnTooltipGenerator_hasFluidAPI() {
        var column = grid.addColumn(item -> item)
                .setTooltipGenerator(item -> item).setAutoWidth(true);
        Assertions.assertTrue(column.isAutoWidth());
    }

    @Test
    void setColumnTooltip_tooltipHasSlot() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assertions.assertEquals("tooltip",
                getTooltipElement(grid).orElseThrow().getAttribute("slot"));
    }

    @Test
    void setGridTooltip_tooltipHasSlot() {
        grid.setTooltipGenerator(item -> item);
        Assertions.assertEquals("tooltip",
                getTooltipElement(grid).orElseThrow().getAttribute("slot"));
    }

    @Test
    void setAnotherColumnTooltipGenerator_hasOneTooltipElement() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assertions.assertEquals(1, getTooltipElements(grid).count());
    }

    @Test
    void setNullColumnTooltipGenerator_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> grid.addColumn(item -> item).setTooltipGenerator(null));
    }

    @Test
    void setNullGridTooltipGenerator_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> grid.setTooltipGenerator(null));
    }

    @Test
    void setTooltipPosition_hasTooltipElement() {
        grid.setTooltipPosition(Tooltip.TooltipPosition.START);
        Assertions.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    void setTooltipPosition_hasTooltipWithPosition() {
        grid.setTooltipPosition(Tooltip.TooltipPosition.START);
        Assertions.assertEquals("start",
                getTooltipElement(grid).orElseThrow().getAttribute("position"));

        grid.setTooltipPosition(Tooltip.TooltipPosition.END);
        Assertions.assertEquals("end",
                getTooltipElement(grid).orElseThrow().getAttribute("position"));
    }

    @Test
    void setTooltipPosition_throwsForNull() {
        Assertions.assertThrows(NullPointerException.class,
                () -> grid.setTooltipPosition(null));
    }

    @Test
    void setTooltipPosition_getTooltipPosition() {
        grid.setTooltipPosition(Tooltip.TooltipPosition.START);
        Assertions.assertEquals(Tooltip.TooltipPosition.START,
                grid.getTooltipPosition());

        grid.setTooltipPosition(Tooltip.TooltipPosition.END);
        Assertions.assertEquals(Tooltip.TooltipPosition.END,
                grid.getTooltipPosition());
    }

    @Test
    void getTooltipPosition_defaultTooltipPosition() {
        // without tooltip element
        Assertions.assertEquals(Tooltip.TooltipPosition.BOTTOM,
                grid.getTooltipPosition());

        // with tooltip element, unspecified position
        grid.setTooltipGenerator(item -> item);
        Assertions.assertEquals(Tooltip.TooltipPosition.BOTTOM,
                grid.getTooltipPosition());

        // with tooltip element, invalid position
        getTooltipElement(grid).orElseThrow().setAttribute("position",
                "invalid");
        Assertions.assertEquals(Tooltip.TooltipPosition.BOTTOM,
                grid.getTooltipPosition());
    }

    @Test
    void setTooltipMarkdownEnabled_hasTooltipElement() {
        grid.setTooltipMarkdownEnabled(true);
        Assertions.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    void setTooltipMarkdownEnabled_hasTooltipWithProperty() {
        grid.setTooltipMarkdownEnabled(true);
        Assertions.assertTrue(getTooltipElement(grid).orElseThrow()
                .getProperty("markdown", false));

        grid.setTooltipMarkdownEnabled(false);
        Assertions.assertFalse(getTooltipElement(grid).orElseThrow()
                .getProperty("markdown", false));
    }

    @Test
    void setTooltipMarkdownEnabled_isTooltipMarkdownEnabled() {
        grid.setTooltipMarkdownEnabled(true);
        Assertions.assertTrue(grid.isTooltipMarkdownEnabled());

        grid.setTooltipMarkdownEnabled(false);
        Assertions.assertFalse(grid.isTooltipMarkdownEnabled());
    }

    @Test
    void isTooltipMarkdownEnabled_defaultValue() {
        // without tooltip element
        Assertions.assertFalse(grid.isTooltipMarkdownEnabled());

        // with tooltip element, no property value
        grid.setTooltipGenerator(item -> item);
        Assertions.assertFalse(grid.isTooltipMarkdownEnabled());
    }

    private Optional<Element> getTooltipElement(Grid<?> grid) {
        return getTooltipElements(grid).findFirst();
    }

    private Stream<Element> getTooltipElements(Grid<?> grid) {
        return grid.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

}
