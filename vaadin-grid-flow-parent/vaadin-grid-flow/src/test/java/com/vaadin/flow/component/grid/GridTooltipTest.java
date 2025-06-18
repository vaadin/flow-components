/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
