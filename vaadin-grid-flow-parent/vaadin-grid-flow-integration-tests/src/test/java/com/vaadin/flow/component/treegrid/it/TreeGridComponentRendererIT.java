/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-component-renderer")
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
        IntStream.range(0, getTreeGrid().getRowCount())
                .forEach(i -> Assert.assertNotNull(
                        "Row with index " + i + " has no component renderer",
                        getTreeGrid().getCell(i, 0).$("vaadin-text-field")));
    }
}
