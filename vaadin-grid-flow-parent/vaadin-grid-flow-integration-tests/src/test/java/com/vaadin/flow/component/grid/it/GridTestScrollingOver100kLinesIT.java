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
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/scroll-over-100k")
public class GridTestScrollingOver100kLinesIT extends AbstractComponentIT {

    @Test
    public void toStringIsUsedForObjectSerialization() {
        open();

        GridElement grid = $(GridElement.class).first();

        TestBenchElement scroller = grid.$("table").id("table");

        // Scroll value that scrolls a bit further than the 100000th row to
        // trigger the loading loop issue (vaadin-grid-flow issue #578)
        scroller.setProperty("scrollTop", 3597800);

        waitUntil(e -> !grid.getPropertyBoolean("loading"));

        // Checks that all header and body cells have text content
        List<TestBenchElement> allCellContents = grid
                .$("vaadin-grid-cell-content").all();
        List<TestBenchElement> headerSlots = grid.$("thead slot").all();
        List<TestBenchElement> bodySlots = grid.$("tbody slot").all();
        Assert.assertTrue(headerSlots.size() > 0);
        Assert.assertTrue(bodySlots.size() > 0);

        allCellContents.forEach(vgcc -> {
            TestBenchElement slot = vgcc.getPropertyElement("assignedSlot");
            if (headerSlots.contains(slot) || bodySlots.contains(slot)) {
                Assert.assertTrue(
                        "A grid cell was expected to have text content but had none.",
                        StringUtils.isNotBlank(vgcc.getText()));
            }
        });
    }
}
