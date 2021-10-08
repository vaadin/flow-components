package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.GridSelectionColumn;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/grid-multi-select")
public class GridMultiSelectIT extends AbstractComponentIT {

    private static final String SELECT_ALL_CHECKBOX_ID = "selectAllCheckbox";
    private GridElement grid;

    @Before
    public void init() {
        open();

        grid = $(GridElement.class).first();
    }

    @Test
    public void selectAllColumn_shouldBeSelected_whenAllRowsSelectedOnServerSide() {
        WebElement selectAllCheckbox = grid
                .findElement(By.id(SELECT_ALL_CHECKBOX_ID));
        Assert.assertEquals("true", selectAllCheckbox.getAttribute("checked"));
    }
}
