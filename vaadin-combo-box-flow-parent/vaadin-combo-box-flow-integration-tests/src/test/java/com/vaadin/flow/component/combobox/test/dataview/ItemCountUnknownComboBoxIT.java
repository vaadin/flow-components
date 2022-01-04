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

package com.vaadin.flow.component.combobox.test.dataview;

import static com.vaadin.flow.component.combobox.test.dataview.AbstractItemCountComboBoxPage.DEFAULT_DATA_PROVIDER_SIZE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.testutil.TestPath;

@TestPath("item-count-unknown")
public class ItemCountUnknownComboBoxIT extends AbstractItemCountComboBoxIT {

    @Test
    public void undefinedItemCount_defaultPageSizeEvenToDatasetItemCount_scrollingToEnd() {
        final int datasetItemCount = 500;
        open(datasetItemCount);

        verifyItemsCount(getDefaultInitialItemCount());
        verifyFetchForUndefinedItemCountCallback(RangeLog.of(0, 0, 50));

        doScroll(45, getDefaultInitialItemCount(), "Callback Item 45");

        // trigger next page fetch and item count buffer increase
        doScroll(150, 400, "Callback Item 150", RangeLog.of(2, 100, 150));

        // jump over a page, trigger fetch
        doScroll(270, 400, "Callback Item 270", RangeLog.of(4, 250, 300));

        // trigger another buffer increase but not capping item count
        doScroll(395, 600, "Callback Item 395", RangeLog.of(6, 400, 450));

        // scroll to actual end, no more items returned and item count is
        // adjusted
        doScroll(499, 500, "Callback Item 499", RangeLog.of(8, 500, 550));

        // scroll to 0 position and check the item count is correct
        doScroll(0, 500, "Callback Item 0", RangeLog.of(9, 0, 50));

        // scroll again to the end of list and check the item count
        doScroll(450, 500, "Callback Item 450", RangeLog.of(10, 400, 450),
                RangeLog.of(11, 450, 500));
    }

    @Test
    public void undefinedItemCount_switchesToDefinedItemCount_itemCountChanges() {
        int actualItemCount = 300;
        open(actualItemCount);

        verifyItemsCount(getDefaultInitialItemCount());
        verifyFetchForUndefinedItemCountCallback(RangeLog.of(0, 0, 50));

        doScroll(150, 400, "Callback Item 150", RangeLog.of(1, 100, 150));

        doScroll(299, actualItemCount, "Callback Item 299",
                RangeLog.of(3, 250, 300), RangeLog.of(4, 300, 350));

        // change callback backend item count limit
        setUnknownCountBackendItemsCount(DEFAULT_DATA_PROVIDER_SIZE);
        // combo box has scrolled to end -> switch to defined count callback
        // -> new count updated and more items fetched
        setCountCallback();

        verifyItemsCount(DEFAULT_DATA_PROVIDER_SIZE);

        // Web component requests first page (dropdown opened)
        verifyFetchForUndefinedItemCountCallback(RangeLog.of(6, 0, 50));

        // Check that combo box is scrolled over 'actualItemCount' after
        // switching to defined items count
        doScroll(500, DEFAULT_DATA_PROVIDER_SIZE, "Callback Item 500",
                RangeLog.of(7, 450, 500), RangeLog.of(8, 500, 550));

        // switching back to undefined items count, nothing changes
        setUnknownCount();

        verifyItemsCount(DEFAULT_DATA_PROVIDER_SIZE);

        // Dropdown opened, since requested first page
        verifyFetchForUndefinedItemCountCallback(RangeLog.of(9, 0, 50));

        doScroll(500, DEFAULT_DATA_PROVIDER_SIZE, "Callback Item 500",
                RangeLog.of(10, 450, 500), RangeLog.of(11, 500, 550));

        // increase backend item count and scroll to current end
        setUnknownCountBackendItemsCount(2000);
        // count has been increased again by default increase value
        doScroll(1000, 1200, "Callback Item 999", RangeLog.of(12, 950, 1000),
                RangeLog.of(13, 1000, 1050));
    }

    @Test
    public void undefinedItemCount_switchesToEstimateCountLargerThanCurrentEstimate_itemCountChanges() {
        open(1000);

        setEstimate(300);

        verifyItemsCount(300);

        verifyFetchForUndefinedItemCountCallback(RangeLog.of(0, 0, 50));

        setEstimate(600);

        // Force updating the combobox items count by scrolling one page forward
        doScroll(99, 600, "Callback Item 99", RangeLog.of(1, 50, 100),
                RangeLog.of(2, 100, 150));
    }

    @Test
    public void undefinedItemCount_switchesToEstimateItemCountLessThanCurrentEstimate_estimateDiscarded() {
        open(1000);

        setEstimate(600);

        verifyItemsCount(600);

        // Change estimation to the lower value is still fine, because it is
        // larger than assumed item count
        setEstimate(300);

        verifyItemsCount(300);

        setEstimate(50);

        // Check that the estimated count does not go to client, because
        // requestedRange.getEnd() > itemCountEstimate, and it does not
        // trigger the item count reset.
        verifyItemsCount(300);
    }

    @Test
    public void undefinedItemCount_enterClientFilter_displaysFilteredItem() {
        open(300);

        assertLoadedItemsCount("Should be 50 items before filtering", 50,
                comboBoxElement);

        // Apply text filter
        comboBoxElement.sendKeys("Callback Item 250", Keys.ENTER);

        waitForItems(comboBoxElement, items -> items.size() == 1
                && "Callback Item 250".equals(getItemLabel(items, 0)));
    }
}
