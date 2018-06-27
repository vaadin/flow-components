package com.vaadin.flow.component.treegrid.it;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-component-renderer")
public class TreeGridComponentRendererIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void treegridComponentRenderer_expandCollapseExpand_renderersShows() {
        getTreeGrid().expandWithClick(0);

        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(1, 0, "Dad 0/0");
        assertCellTexts(2, 0, "Dad 0/1");
        assertCellTexts(3, 0, "Dad 0/2");
        assertCellTexts(4, 0, "Granddad 1");
        assertCellTexts(5, 0, "Granddad 2");

        assertAllRowsHasTextField(6);

        getTreeGrid().collapseWithClick(0);

        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(1, 0, "Granddad 1");
        assertCellTexts(2, 0, "Granddad 2");

        assertAllRowsHasTextField(3);

        getTreeGrid().expandWithClick(0);

        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(1, 0, "Dad 0/0");
        assertCellTexts(2, 0, "Dad 0/1");
        assertCellTexts(3, 0, "Dad 0/2");
        assertCellTexts(4, 0, "Granddad 1");
        assertCellTexts(5, 0, "Granddad 2");

        assertAllRowsHasTextField(6);
    }

    private void assertAllRowsHasTextField(int expectedRowCount) {
        Assert.assertEquals(expectedRowCount, getTreeGrid().getRowCount());
        IntStream.range(0, getTreeGrid().getRowCount()).forEach(
                i -> Assert.assertTrue(getTreeGrid().hasComponentRenderer(i, 1,
                        By.tagName("vaadin-text-field"))));
    }
}
