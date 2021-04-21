/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

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
    public void test_focusFirstCell() {
        open();

        getGrid().getCell(0, 0).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT, "A");

        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);

        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_DETAILS);


        getGrid().getCell(1, 0).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT, "B");
        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_DETAILS);

        getGrid().getCell(2, 1).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT, "C");
        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_SECOND_COLUMN);
        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_DETAILS);
    }

    @Test
    public void test_focusHeaderCell() {
        open();

        getGrid().getHeaderCell(0).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_HEADER);

        getGrid().getHeaderCell(1).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_SECOND_COLUMN);
        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_HEADER);

    }

    @Test
    public void test_focusFooterCell() {
        open();

        getGrid().getFooterCell(0).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_FIRST_COLUMN);
        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_FOOTER);

        getGrid().getFooterCell(1).focus();
        assertItemResult(GridCellFocusPage.ID_ITEM_RESULT,
                GridCellFocusPage.NO_ITEM);
        assertItemResult(GridCellFocusPage.ID_COLUMN_RESULT,
                GridCellFocusPage.KEY_SECOND_COLUMN);
        assertItemResult(GridCellFocusPage.ID_SECTION_RESULT,
                GridCellFocusPage.SECTION_FOOTER);
    }

    private GridElement getGrid() {
        return $(GridElement.class).id(GridCellFocusPage.ID_GRID);
    }

    private void assertItemResult(String resultFieldId, String expectedResult) {
        String text = $(TestBenchElement.class)
                .id(resultFieldId)
                .getText();

        Assert.assertEquals(expectedResult, text);
    }
}
