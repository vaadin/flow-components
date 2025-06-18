/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
