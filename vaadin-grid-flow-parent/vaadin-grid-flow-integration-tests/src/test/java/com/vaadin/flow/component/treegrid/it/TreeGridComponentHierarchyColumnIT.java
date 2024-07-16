/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-component-hierarchy-column")
public class TreeGridComponentHierarchyColumnIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void treegridComponentRenderer_expandCollapseExpand_renderersShows() {

        assertAllRowsHasTextField(3);

        getTreeGrid().expandWithClick(0);
        assertAllRowsHasTextField(6);

        getTreeGrid().collapseWithClick(0);
        assertAllRowsHasTextField(3);

        getTreeGrid().expandWithClick(0);
        assertAllRowsHasTextField(6);
    }

    private void assertAllRowsHasTextField(int expectedRowCount) {
        Assert.assertEquals(expectedRowCount, getTreeGrid().getRowCount());
        IntStream.range(0, getTreeGrid().getRowCount())
                .forEach(i -> Assert.assertTrue(
                        "Row with index " + i + " has no component renderer",
                        getTreeGrid().hasComponentRenderer(i, 0,
                                By.tagName("vaadin-text-field"))));
    }
}
