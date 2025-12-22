/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/treegrid-scroll-position")
public class TreeGridScrollPositionIT extends AbstractComponentIT {
    private TreeGridElement treeGrid;

    @Before
    public void init() {
        open();
        treeGrid = $(TreeGridElement.class).first();
    }

    @Test
    public void setRowsDraggable_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-rows-draggable");
        });
    }

    @Test
    public void setDragFilter_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-drag-filter");
        });
    }

    @Test
    public void setDropFilter_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-drop-filter");
        });
    }

    @Test
    public void setDropMode_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-drop-mode");
        });
    }

    @Test
    public void setDragDataGenerator_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-drag-data-generator");
        });
    }

    @Test
    public void setPartNameGenerator_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-part-name-generator");
        });
    }

    @Test
    public void setTooltipGenerator_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-tooltip-generator");
        });
    }

    @Test
    public void addColumn_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("add-column");
        });
    }

    @Test
    public void setColumnCustomRenderer_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-column-custom-renderer");
        });
    }

    @Test
    public void setColumnTooltipGenerator_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-column-tooltip-generator");
        });
    }

    @Test
    public void setColumnPartNameGenerator_scrollPositionNotChanged() {
        assertScrollPositionNotChanged(() -> {
            clickElementWithJs("set-column-part-name-generator");
        });
    }

    public void assertScrollPositionNotChanged(SerializableRunnable action) {
        var firstVisibleRowIndex = treeGrid.getFirstVisibleRowIndex();
        action.run();
        Assert.assertEquals(firstVisibleRowIndex,
                treeGrid.getFirstVisibleRowIndex());
    }
}
