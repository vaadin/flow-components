
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

@TestPath("vaadin-grid/lazy-loading-treegrid-refreshall")
public class LazyLoadingTreeGridRefreshAllIT extends AbstractTreeGridIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setChildCountAndRefreshAll_resultIsNotEmpty() {
        TreeGridElement treeGrid = $(TreeGridElement.class).get(0);

        // The row index should be larger than 100 in order to replicate the
        // issue.
        treeGrid.scrollToRow(120);

        // The count should be smaller than 100 in order to replicate the issue.
        IntegerFieldElement childCount = $(IntegerFieldElement.class)
                .id("child-count");
        childCount.setValue("5");

        ButtonElement refreshAll = $(ButtonElement.class).id("refresh-all");
        refreshAll.click();

        Assert.assertTrue(treeGrid.getRowCount() > 0);
    }
}
