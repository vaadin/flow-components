/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-grid/beangridpage")
public class BeanGridIT extends AbstractComponentIT {

    @Test
    public void gridNullValuesRenderedAsEmptyStrings() {
        open();
        GridElement grid = $(GridElement.class).first();
        String text = grid.getText();
        Assert.assertFalse("Null values should be presented as empty strings",
                text.contains("null"));
    }

    @Test
    public void rowCanBeDeselectedOnSingleSelectMode() {
        open();
        GridElement grid = $(GridElement.class).first();

        // Select first row.
        assertRowSelectionStatus(grid, 0, false);
        grid.select(0);
        assertRowSelectionStatus(grid, 0, true);

        // Try to deselect the wrong row
        grid.deselect(1);
        assertRowSelectionStatus(grid, 0, true);

        // Deselect the first row
        grid.deselect(0);
        assertRowSelectionStatus(grid, 0, false);
    }

    private void assertRowSelectionStatus(GridElement grid, int rowIndex,
            boolean expectedStatus) {
        Assert.assertEquals("Unexpected selection status for row " + rowIndex,
                expectedStatus, grid.getRow(rowIndex).isSelected());
    }
}
