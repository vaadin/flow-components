/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

public class GridEmptyStateTest {

    private Grid<String> grid;

    @Before
    public void setup() {
        grid = new Grid<>();
    }

    @Test
    public void setEmptyStateComponent_hasEmptyStateComponent() {
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assert.assertEquals(content.getElement(), getEmptyStateElement());
    }

    @Test
    public void setEmptyStateText_hasEmptyStateText() {
        var content = "empty";
        grid.setEmptyStateText(content);
        var emptyStateElement = getEmptyStateElement();
        Assert.assertEquals(content, emptyStateElement.getText());
        Assert.assertEquals("span", emptyStateElement.getTag());
    }

    @Test
    public void setEmptyStateComponent_overridesEmptyStateText() {
        grid.setEmptyStateText("empty");
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assert.assertEquals(content.getElement(), getEmptyStateElement());
    }

    @Test
    public void setEmptyStateText_overridesEmptyStateComponent() {
        var content = "empty";
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText(content);
        var emptyStateElement = getEmptyStateElement();
        Assert.assertEquals(content, emptyStateElement.getText());
        Assert.assertEquals("span", emptyStateElement.getTag());
    }

    @Test
    public void setEmptyStateComponentNull_noEmptyStateComponent() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(null);
        Assert.assertTrue(getEmptyStateElementOptional().isEmpty());
    }

    @Test
    public void setEmptyStateTextNull_noEmptyStateComponent() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateText(null);
        Assert.assertTrue(getEmptyStateElementOptional().isEmpty());
    }

    @Test
    public void setEmptyStateComponent_getEmptyStateComponent() {
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assert.assertEquals(content, grid.getEmptyStateComponent());
    }

    @Test
    public void setEmptyStateText_getEmptyStateText() {
        var content = "empty";
        grid.setEmptyStateText(content);
        Assert.assertEquals(content, grid.getEmptyStateText());
    }

    @Test
    public void setEmptyStateComponent_setEmptyStateText_getEmptyStateComponent() {
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText("empty");
        Assert.assertNull(grid.getEmptyStateComponent());
    }

    @Test
    public void setEmptyStateText_setEmptyStateComponent_getEmptyStateText() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(new Div());
        Assert.assertNull(grid.getEmptyStateText());
    }

    @Test
    public void setEmptyStateComponent_setEmptyStateTextNull_getEmptyStateComponent() {
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText(null);
        Assert.assertNull(grid.getEmptyStateComponent());
        Assert.assertNull(grid.getEmptyStateText());
    }

    @Test
    public void setEmptyStateText_setEmptyStateComponentNull_getEmptyStateText() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(null);
        Assert.assertNull(grid.getEmptyStateComponent());
        Assert.assertNull(grid.getEmptyStateText());
    }

    private Element getEmptyStateElement() {
        return getEmptyStateElementOptional().orElse(null);
    }

    private Optional<Element> getEmptyStateElementOptional() {
        return grid.getElement().getChildren().filter(
                child -> child.getAttribute("slot").equals("empty-state"))
                .findFirst();
    }
}
