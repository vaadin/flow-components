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

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/treegrid-filter-expand")
public class TreeGridFilterExpandIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test // https://github.com/vaadin/vaadin-grid-flow/issues/891
    public void filterAndExpandAllMultipleTimes_correctItemsShown()
            throws InterruptedException {
        TestBenchElement filterField = $("input").first();

        // Typing two letters with ValueChangeMode.EAGER on the field to trigger
        // two filtering updates.
        filterField.sendKeys("ap");

        // Because of the way that the connector forcefully updates grid's
        // cache, and the incorrect updates are coming asynchronously, there
        // seems to be no way of checking when all the requests have been
        // responded, and no more forceful updates are coming.
        Thread.sleep(2000);

        String[] expectedTextContents = new String[] { "transportation", "map",
                "personnel logistics", "request & approval",
                "group schedule: request & approval",
                "approve purpose of visit", "approve replacement" };

        Assert.assertArrayEquals(expectedTextContents, getActualTextContents());
    }

    private String[] getActualTextContents() {
        return IntStream.range(0, getTreeGrid().getRowCount())
                .mapToObj(i -> getTreeGrid().getCell(i, 0).getText())
                .toArray(String[]::new);
    }

}
