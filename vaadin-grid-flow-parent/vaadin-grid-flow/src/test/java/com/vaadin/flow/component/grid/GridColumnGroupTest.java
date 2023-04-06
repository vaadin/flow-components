
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

public class GridColumnGroupTest {
    @Test
    public void templateWarningSuppressed() {
        Grid<String> grid = new Grid<>();

        ColumnGroup columnGroup = new ColumnGroup(grid);

        Assert.assertTrue("Template warning is not suppressed", columnGroup
                .getElement().hasAttribute("suppress-template-warning"));
    }
}
