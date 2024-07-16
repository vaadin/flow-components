/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.CellFocusEvent;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * IT for grid's Flow based cell focus event.
 *
 * @author Vaadin Ltd
 */

@TestPath("vaadin-grid/grid-cell-focus-page")
public class GridCellFocusPageIT extends AbstractComponentIT {

    @Test
    public void focusBodyCell() {
        open();

        getGrid().getCell(0, 0).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT, "A");
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.BODY.getClientSideName());

        getGrid().getCell(1, 0).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT, "B");
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.BODY.getClientSideName());

        getGrid().getCell(2, 1).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT, "C");
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_SECOND_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.BODY.getClientSideName());
    }

    @Test
    public void focusHeaderCell() {
        open();

        getGrid().getHeaderCell(0).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.HEADER.getClientSideName());

        getGrid().getHeaderCell(1).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_SECOND_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.HEADER.getClientSideName());
    }

    @Test
    public void focusFooterCell() {
        open();

        getGrid().getFooterCell(0).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.FOOTER.getClientSideName());

        getGrid().getFooterCell(1).click(0, 0);
        assertTextResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertTextResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_SECOND_COLUMN);
        assertTextResult(GridCellFocusPage.ID_SECTION_RESULT,
                CellFocusEvent.GridSection.FOOTER.getClientSideName());
    }

    private GridElement getGrid() {
        return $(GridElement.class).id(GridCellFocusPage.ID_GRID);
    }

    private void assertTextResult(String resultFieldId, String expectedResult) {
        String text = $(TestBenchElement.class).id(resultFieldId).getText();

        Assert.assertEquals(expectedResult, text);
    }
}
