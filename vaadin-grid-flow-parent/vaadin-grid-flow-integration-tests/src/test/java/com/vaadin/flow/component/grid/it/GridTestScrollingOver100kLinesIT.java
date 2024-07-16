/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
        allCellContents.forEach(vgcc -> {
            String slotName = vgcc.getAttribute("slot")
                    .replace("vaadin-grid-cell-content-", "");
            if (Integer.parseInt(slotName) <= 180) {
                Assert.assertTrue(
                        "A grid cell was expected to have text content but had none.",
                        StringUtils.isNotBlank(vgcc.getText()));
            }
        });
    }
}