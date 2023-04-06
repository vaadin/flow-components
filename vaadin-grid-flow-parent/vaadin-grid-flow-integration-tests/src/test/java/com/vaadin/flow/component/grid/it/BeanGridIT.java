
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
