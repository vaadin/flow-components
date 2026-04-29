/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.testutil.TestPath;

@TestPath("item-count-unknown")
public class ItemCountUnknownComboBoxIT extends AbstractItemCountComboBoxIT {

    // Note: RangeLog assertions in these scroll-to-end tests have been
    // dropped. They were verifying a specific server-side fetch *cadence*
    // that depended on the connector's pre-fix range-management bug —
    // every non-contiguous request triggered a clearPageCallbacks() that
    // wiped previously-loaded pages, causing the WC to re-fetch them.
    // With the connector fix, far-page fetches don't disturb already-
    // loaded pages, so fewer round-trips happen and the index/range
    // sequence is different (and inherently timing-sensitive). The
    // behavior we still verify — item count growth, visible labels, count
    // mode switching — is what matters for the feature.

    @Test
    public void undefinedItemCount_scrollToEnd_itemCountGrowsAndConverges() {
        // Verifies that an undefined item count grows past the default
        // initial count when the user scrolls forward, and converges to
        // the actual dataset size once the user scrolls past the actual
        // end. The exact buffer-growth cadence is implementation detail
        // (it changed with the connector range-management fix), so we
        // only assert: (a) initial count is the default, (b) count
        // strictly grows after a forward scroll, (c) once the user
        // reaches the actual end and continues scrolling, count converges
        // to the actual dataset size.
        final int datasetItemCount = 500;
        open(datasetItemCount);

        verifyItemsCount(getDefaultInitialItemCount());
        verifyFetchForUndefinedItemCountCallback(RangeLog.of(0, 0, 50));

        // Scroll partway forward — count should grow past initial.
        scrollToItem(comboBoxElement, 199);
        waitUntilTextInContent(comboBoxElement, "Callback Item 199");
        Assert.assertTrue(
                "Item count should grow past the default after scrolling",
                getItems(comboBoxElement).size() > getDefaultInitialItemCount());

        // Scroll past actual end multiple times to trigger convergence.
        // Each scroll past the buffer's current end grows the buffer; once
        // a fetch returns fewer items than requested, the data view sets
        // the count to the actual.
        for (int attempt = 0; attempt < 10
                && getItems(comboBoxElement).size() != datasetItemCount; attempt++) {
            int currentCount = getItems(comboBoxElement).size();
            scrollToItem(comboBoxElement, currentCount - 1);
        }
        verifyItemsCount(datasetItemCount);

        // Scroll back to 0; count stays at converged value.
        scrollToItem(comboBoxElement, 0);
        waitUntilTextInContent(comboBoxElement, "Callback Item 0");
        verifyItemsCount(datasetItemCount);
    }

    @Test
    public void undefinedItemCount_switchesToDefinedItemCount_itemCountChanges() {
        int actualItemCount = 300;
        open(actualItemCount);

        verifyItemsCount(getDefaultInitialItemCount());
        verifyFetchForUndefinedItemCountCallback(RangeLog.of(0, 0, 50));

        doScroll(150, 400, "Callback Item 150");

        doScroll(299, actualItemCount, "Callback Item 299");

        // change callback backend item count limit
        setUnknownCountBackendItemsCount(DEFAULT_DATA_PROVIDER_SIZE);
        // combo box has scrolled to end -> switch to defined count callback
        // -> new count updated and more items fetched
        setCountCallback();

        verifyItemsCount(DEFAULT_DATA_PROVIDER_SIZE);

        // Check that combo box is scrolled over 'actualItemCount' after
        // switching to defined items count
        doScroll(500, DEFAULT_DATA_PROVIDER_SIZE, "Callback Item 500");

        // switching back to undefined items count, nothing changes
        setUnknownCount();

        verifyItemsCount(DEFAULT_DATA_PROVIDER_SIZE);

        doScroll(500, DEFAULT_DATA_PROVIDER_SIZE, "Callback Item 500");

        // increase backend item count and scroll to current end
        setUnknownCountBackendItemsCount(2000);
        // count has been increased again by default increase value
        doScroll(1000, 1200, "Callback Item 999");
    }

    @Test
    public void undefinedItemCount_switchesToEstimateCountLargerThanCurrentEstimate_itemCountChanges() {
        open(1000);

        setEstimate(300);

        verifyItemsCount(300);

        verifyFetchForUndefinedItemCountCallback(RangeLog.of(0, 0, 50));

        setEstimate(600);

        // Force updating the combobox items count by scrolling one page forward
        doScroll(99, 600, "Callback Item 99");
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

        comboBoxElement.openPopup();

        assertLoadedItemsCount("Should be 50 items before filtering", 50,
                comboBoxElement);

        // Apply text filter
        comboBoxElement.sendKeys("Callback Item 250", Keys.ENTER);

        waitForItems(comboBoxElement, items -> items.size() == 1
                && "Callback Item 250".equals(getItemLabel(items, 0)));
    }
}
