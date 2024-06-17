package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/empty-state")
public class GridEmptyStateIT extends AbstractComponentIT {
    private GridElement grid;
    private ButtonElement setEmptyStateContentButton;
    private ButtonElement clearItemsButton;
    private ButtonElement setItemsButton;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        setEmptyStateContentButton = $(ButtonElement.class)
                .id("set-empty-state-content");
        clearItemsButton = $(ButtonElement.class).id("clear-items");
        setItemsButton = $(ButtonElement.class).id("set-items");
    }

    @Test
    public void getEmptyStateContent_throws() {
        Assert.assertThrows("No empty state content was found",
                NoSuchElementException.class,
                () -> grid.getEmptyStateContent());
    }

    @Test
    public void setEmptyStateContent_emptyStateContentDisplayed() {
        setEmptyStateContentButton.click();
        var content = grid.getEmptyStateContent();
        Assert.assertEquals("Custom empty state content", content.getText());
        Assert.assertTrue(content.isDisplayed());
    }

    @Test
    public void setEmptyStateContent_setItems_emptyStateContentNotDisplayed() {
        setEmptyStateContentButton.click();
        setItemsButton.click();
        var content = grid.getEmptyStateContent();
        Assert.assertFalse(content.isDisplayed());
    }

    @Test
    public void setEmptyStateContent_setItems_clearItems_emptyStateContentDisplayed() {
        setEmptyStateContentButton.click();
        setItemsButton.click();
        clearItemsButton.click();
        var content = grid.getEmptyStateContent();
        Assert.assertTrue(content.isDisplayed());
    }
}
