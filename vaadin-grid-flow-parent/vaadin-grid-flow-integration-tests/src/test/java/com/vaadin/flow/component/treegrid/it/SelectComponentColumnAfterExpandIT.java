/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.grid.it.SelectComponentColumnAfterExpandPage;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath(SelectComponentColumnAfterExpandPage.VIEW)
public class SelectComponentColumnAfterExpandIT extends AbstractTreeGridIT {

    /**
     * <a href="https://github.com/vaadin/vaadin-flow-components/issues/376">
     * See issue 376 in vaadin-flow-components </a>
     */
    @Test
    public void select_after_expand_should_not_remove_item_text() {
        open();
        setupTreeGrid();
        assertExpectedValuesWhenExpanded();
        Assert.assertEquals(4, getTreeGrid().getRowCount());
        click("collapse-button");
        Assert.assertEquals(1, getTreeGrid().getRowCount());
        click("expand-button");
        click("select-button");
        assertExpectedValuesWhenExpanded();
    }

    private void click(String id) {
        findElement(By.id(id)).click();
    }

    private void assertCellText(int rowIndex, int collIndex, String expected) {
        Assert.assertEquals(expected,
                getTreeGrid().getCellWaitForRow(rowIndex, collIndex).getText());
    }

    private void assertRowText(int rowIndex, String expected) {
        assertCellText(rowIndex, 0, expected);
        assertCellText(rowIndex, 1, expected);
    }

    private void assertExpectedValuesWhenExpanded() {
        assertRowText(0, "Root");
        assertRowText(1, "child");
        assertRowText(2, "sub-child");
        assertRowText(3, "child2");
    }
}
