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
package com.vaadin.flow.component.grid;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Element;

/**
 * Unit tests for Grid tooltip.
 *
 * @author Vaadin Ltd.
 */
public class GridTooltipTest {

    private Grid<String> grid;

    @Before
    public void setup() {
        grid = new Grid<>();
        grid.addColumn(item -> item);
    }

    @Test
    public void default_doesNotHaveTooltipElement() {
        Assert.assertFalse(getTooltipElement(grid).isPresent());
    }

    @Test
    public void setColumnTooltipGenerator_hasTooltipElement() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assert.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    public void setGridTooltipGenerator_hasTooltipElement() {
        grid.setTooltipGenerator(item -> item);
        Assert.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    public void setColumnTooltipGenerator_hasFluidAPI() {
        var column = grid.addColumn(item -> item)
                .setTooltipGenerator(item -> item).setAutoWidth(true);
        Assert.assertTrue(column.isAutoWidth());
    }

    @Test
    public void setColumnTooltip_tooltipHasSlot() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assert.assertEquals("tooltip",
                getTooltipElement(grid).orElseThrow().getAttribute("slot"));
    }

    @Test
    public void setGridTooltip_tooltipHasSlot() {
        grid.setTooltipGenerator(item -> item);
        Assert.assertEquals("tooltip",
                getTooltipElement(grid).orElseThrow().getAttribute("slot"));
    }

    @Test
    public void setAnotherColumnTooltipGenerator_hasOneTooltipElement() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assert.assertEquals(1, getTooltipElements(grid).count());
    }

    @Test(expected = NullPointerException.class)
    public void setNullColumnTooltipGenerator_throws() {
        grid.addColumn(item -> item).setTooltipGenerator(null);
    }

    @Test(expected = NullPointerException.class)
    public void setNullGridTooltipGenerator_throws() {
        grid.setTooltipGenerator(null);
    }

    @Test
    public void setTooltipPosition_hasTooltipElement() {
        grid.setTooltipPosition(Tooltip.TooltipPosition.START);
        Assert.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    public void setTooltipPosition_hasTooltipWithPosition() {
        grid.setTooltipPosition(Tooltip.TooltipPosition.START);
        Assert.assertEquals("start",
                getTooltipElement(grid).orElseThrow().getAttribute("position"));

        grid.setTooltipPosition(Tooltip.TooltipPosition.END);
        Assert.assertEquals("end",
                getTooltipElement(grid).orElseThrow().getAttribute("position"));
    }

    @Test
    public void setTooltipPosition_throwsForNull() {
        Assert.assertThrows(NullPointerException.class,
                () -> grid.setTooltipPosition(null));
    }

    @Test
    public void setTooltipPosition_getTooltipPosition() {
        grid.setTooltipPosition(Tooltip.TooltipPosition.START);
        Assert.assertEquals(Tooltip.TooltipPosition.START,
                grid.getTooltipPosition());

        grid.setTooltipPosition(Tooltip.TooltipPosition.END);
        Assert.assertEquals(Tooltip.TooltipPosition.END,
                grid.getTooltipPosition());
    }

    @Test
    public void getTooltipPosition_defaultTooltipPosition() {
        // without tooltip element
        Assert.assertEquals(Tooltip.TooltipPosition.BOTTOM,
                grid.getTooltipPosition());

        // with tooltip element, unspecified position
        grid.setTooltipGenerator(item -> item);
        Assert.assertEquals(Tooltip.TooltipPosition.BOTTOM,
                grid.getTooltipPosition());

        // with tooltip element, invalid position
        getTooltipElement(grid).orElseThrow().setAttribute("position",
                "invalid");
        Assert.assertEquals(Tooltip.TooltipPosition.BOTTOM,
                grid.getTooltipPosition());
    }

    private Optional<Element> getTooltipElement(Grid<?> grid) {
        return getTooltipElements(grid).findFirst();
    }

    private Stream<Element> getTooltipElements(Grid<?> grid) {
        return grid.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

}
