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

package com.vaadin.flow.component.grid.dataview;

import com.vaadin.flow.internal.Range;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

import static com.vaadin.flow.component.grid.it.dataview.AbstractItemCountGridPage.DEFAULT_DATA_PROVIDER_SIZE;

@TestPath("vaadin-grid/item-count-unknown")
public class ItemCountUnknownGridIT extends AbstractItemCountGridIT {

    @Test
    public void undefinedSizeGrid_defaultPageSizeEvenToDatasetSize_scrollingToEnd() {
        final int datasetSize = 500;
        open(datasetSize);

        verifyRows(getDefaultInitialItemCount());
        verifyFetchForUndefinedSizeCallback(0, Range.withLength(0, pageSize));

        doScroll(45, getDefaultInitialItemCount(), 1, 50, 150);

        // trigger next page fetch and size buffer increase
        doScroll(125, 400, 2, 150, 200);

        // jump over a page, trigger fetch
        doScroll(270, 400, 3, 250, 350);

        // trigger another buffer increase but not capping size
        doScroll(395, 600, 4, 350, 450);

        // scroll to actual end, no more items returned and size is adjusted
        doScroll(500, 500, 5, 450, 500);
        Assert.assertEquals(499, grid.getLastVisibleRowIndex());
        // TODO #1038 test further after grid is note fetching extra stuff when
        // size has been adjusted to less than what it is
        // doScroll(0, 500, 6, 0, 100);
        // doScroll(450, 500, 7, 400, 500);
    }

    @Test
    public void undefinedSizeGrid_switchesToDefinedSize_sizeChanges() {
        int actualSize = 300;
        open(actualSize);

        verifyRows(getDefaultInitialItemCount());
        verifyFetchForUndefinedSizeCallback(0, Range.withLength(0, pageSize));

        doScroll(120, 400, 1, 50, 200);

        doScroll(299, actualSize, 2, 150, actualSize);

        Assert.assertEquals(299, grid.getLastVisibleRowIndex());

        // change callback backend size limit
        setUnknownCountBackendSize(DEFAULT_DATA_PROVIDER_SIZE);
        // grid has scrolled to end -> switch to defined size callback
        // -> new size updated and more items fetched
        setCountCallback();

        verifyRows(DEFAULT_DATA_PROVIDER_SIZE);
        // new rows are added to end due to size increase
        Assert.assertEquals(299, grid.getLastVisibleRowIndex());

        grid.scrollToRow(500);

        int viewportItemCapacity = grid.getLastVisibleRowIndex()
                - grid.getFirstVisibleRowIndex();
        int expectedLastItem = 500 + viewportItemCapacity;
        Assert.assertEquals(
                "Grid should be able to scroll after changing to defined size",
                expectedLastItem, grid.getLastVisibleRowIndex());

        // switching back to undefined size, nothing changes
        setUnknownCount();

        verifyRows(DEFAULT_DATA_PROVIDER_SIZE);
        Assert.assertEquals(expectedLastItem, grid.getLastVisibleRowIndex());

        // increase backend size and scroll to current end
        setUnknownCountBackendSize(2000);
        // size has been increased again by default size
        doScroll(1000, 1200, 6, 950, 1100);

        Assert.assertEquals(1000 + viewportItemCapacity,
                grid.getLastVisibleRowIndex());
    }

    // @Test TODO
    public void undefinedSizeGrid_switchesToInitialEstimateSizeLargerThanCurrentEstimate_sizeChanges() {

    }

    // @Test TODO
    public void undefinedSizeGrid_switchesToInitialEstimateSizeLessThanCurrentEstimate_estimateDiscarded() {

    }

    // @Test TODO
    public void undefinedSizeGrid_switchesToEstimateCallback_sizeChanges() {

    }

    // @Test TODO
    public void undefinedSizeGrid_switchesToEstimateCallbackSizeLessThanCurrent_throws() {

    }
}
