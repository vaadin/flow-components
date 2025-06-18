/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.dataview;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/item-count-callback")
public class ItemCountCallbackGridIT extends AbstractItemCountGridIT {

    @Test
    public void itemCountCallbackCallbackGrid_scrolledToMiddleAndSwitchesToUndefinedSize_canScrollPastOldKnownSize() {
        open(500);

        grid.scrollToRow(250);

        verifyRows(500);

        setUnknownCountBackendSize(1000);
        setUnknownCount();

        verifyRows(500);

        grid.scrollToRow(500);

        verifyRows(700);
    }

    @Test
    public void itemCountCallbackCallbackGrid_scrolledToEndAndSwitchesToUndefinedSize_sizeIsIncreased() {
        open(5800);

        verifyRows(5800);

        grid.scrollToRow(5800);

        Assert.assertEquals(5799, grid.getLastVisibleRowIndex());

        setUnknownCountBackendSize(10000);
        setUnknownCount();

        verifyRows(6000);

        grid.scrollToRow(6000);

        verifyRows(6200);
    }

}
