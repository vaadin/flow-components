/*
 * Copyright 2000-2023 Vaadin Ltd.
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
        grid = new Grid<String>();
        grid.addColumn(item -> item);
    }

    @Test
    public void default_doesNotHaveTooltipElement() {
        Assert.assertFalse(getTooltipElement(grid).isPresent());
    }

    @Test
    public void setTooltipGenerator_hasTooltipElement() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assert.assertTrue(getTooltipElement(grid).isPresent());
    }

    @Test
    public void setTooltipGenerator_hasFluidAPI() {
        var column = grid.addColumn(item -> item)
                .setTooltipGenerator(item -> item).setAutoWidth(true);
        Assert.assertTrue(column.isAutoWidth());
    }

    @Test
    public void setTooltip_tooltipHasSlot() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assert.assertEquals("tooltip",
                getTooltipElement(grid).get().getAttribute("slot"));
    }

    @Test
    public void setAnotherTooltipGenerator_hasOneTooltipElement() {
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        grid.addColumn(item -> item).setTooltipGenerator(item -> item);
        Assert.assertEquals(1, getTooltipElements(grid).count());
    }

    @Test(expected = NullPointerException.class)
    public void setNullTooltipGenerator_throws() {
        grid.addColumn(item -> item).setTooltipGenerator(null);
    }

    private Optional<Element> getTooltipElement(Grid<?> grid) {
        return getTooltipElements(grid).findFirst();
    }

    private Stream<Element> getTooltipElements(Grid<?> grid) {
        return grid.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

}
