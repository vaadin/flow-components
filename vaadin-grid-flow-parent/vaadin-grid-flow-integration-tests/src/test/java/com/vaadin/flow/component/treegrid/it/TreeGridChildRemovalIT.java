/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-grid/tree-grid-child-removal")
public class TreeGridChildRemovalIT extends AbstractTreeGridIT {
    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    /**
     * Refreshing an item recursively should remove from display all elements
     * that where removed on the server-side, including sub-children. See
     * <a href="https://github.com/vaadin/vaadin-grid/issues/1819">Issue
     * 1819</a>.
     */
    @Test
    public void updatingRecursivelyAfterRemovingShouldRemoveAllDeleteItems() {
        // Initial state
        assertCellTexts(0, 0, new String[] { "Root", "child", "Sub-child",
                "child2", "Sub-child2", "child3" });

        // Remove child and refresh "Root" recursively
        $("button").id("remove1").click();
        // Expected result: "child" and "Sub-child" are removed. Others
        // unchanged
        assertCellTexts(0, 0,
                new String[] { "Root", "child2", "Sub-child2", "child3" });

        // Remove child2 and refresh "Root" recursively
        $("button").id("remove2").click();
        // Expected result: "child2" and "Sub-child2" are removed. Others
        // unchanged
        assertCellTexts(0, 0, new String[] { "Root", "child3" });

    }
}
