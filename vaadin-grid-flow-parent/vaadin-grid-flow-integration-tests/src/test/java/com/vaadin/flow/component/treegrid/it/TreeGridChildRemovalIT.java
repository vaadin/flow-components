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
