/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import org.junit.Test;

import com.vaadin.flow.internal.Range;
import com.vaadin.flow.testutil.TestPath;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.combobox.test.dataview.AbstractItemCountComboBoxPage.DEFAULT_DATA_PROVIDER_SIZE;

@TestPath("item-count-unknown")
public class ItemCountUnknownComboBoxIT extends AbstractItemCountComboBoxIT {

    @Test
    public void undefinedSize_defaultPageSizeEvenToDatasetSize_scrollingToEnd() {
        final int datasetSize = 500;
        open(datasetSize);

        verifyItemsSize(getDefaultInitialItemCount());
        verifyFetchForUndefinedSizeCallback(0, Range.withLength(0, pageSize));

        doScroll(45, getDefaultInitialItemCount(), 1, 50, 100,
                "Callback Item 45");

        // trigger next page fetch and size buffer increase
        doScroll(120, 400, 2, 100, 200, "Callback Item 120");

        // jump over a page, trigger fetch
        doScroll(270, 400, 3, 250, 250, "Callback Item 270");

        // trigger another buffer increase but not capping size
        doScroll(395, 600, 4, 250, 350, "Callback Item 395");

        // scroll to actual end, no more items returned and size is adjusted
        doScroll(499, 500, 5, 450, 500, "Callback Item 499");

        // scroll to 0 position and check the size is correct
        doScroll(0, 500, 6, 0, 100, "Callback Item 0");

        // scroll again to the end of list and check the size
        doScroll(450, 500, 7, 400, 500, "Callback Item 450");
    }

    @Test
    public void undefinedSize_switchesToDefinedSize_sizeChanges() {
        int actualSize = 300;
        open(actualSize);

        verifyItemsSize(getDefaultInitialItemCount());
        verifyFetchForUndefinedSizeCallback(0, Range.withLength(0, pageSize));

        doScroll(120, 400, 1, 50, 200, "Callback Item 120");

        doScroll(299, actualSize, 2, 150, actualSize, "Callback Item 299");

        // change callback backend size limit
        setUnknownCountBackendSize(DEFAULT_DATA_PROVIDER_SIZE);
        // combo box has scrolled to end -> switch to defined size callback
        // -> new size updated and more items fetched
        setCountCallback();

        verifyItemsSize(DEFAULT_DATA_PROVIDER_SIZE);

        // Check that combo box is scrolled over 'actualSize' after switching
        // to defined size
        doScroll(500, DEFAULT_DATA_PROVIDER_SIZE, 2, 150, actualSize,
                "Callback Item 500");

        // switching back to undefined size, nothing changes
        setUnknownCount();

        verifyItemsSize(DEFAULT_DATA_PROVIDER_SIZE);
        doScroll(500, DEFAULT_DATA_PROVIDER_SIZE, 2, 150, actualSize,
                "Callback Item 500");

        // increase backend size and scroll to current end
        setUnknownCountBackendSize(2000);
        // size has been increased again by default size
        doScroll(1000, 1200, 6, 950, 1100, "Callback Item 999");
    }

    @Test
    public void undefinedSize_switchesToEstimateSizeLargerThanCurrentEstimate_sizeChanges() {
        open(1000);

        setEstimate(300);

        verifyItemsSize(300);

        setEstimate(600);

        // Force updating the combobox items size by scrolling one page forward
        doScroll(99, 600, 1, 0, 100, "Callback Item 99");
    }

    @Test
    public void undefinedSize_switchesToEstimateSizeLessThanCurrentEstimate_estimateDiscarded() {
        open(1000);

        setEstimate(600);

        verifyItemsSize(600);

        // Change estimation to the lower value is still fine, because it is
        // larger than assumed size
        setEstimate(300);

        verifyItemsSize(300);

        setEstimate(50);

        // Check that the estimated size does not go to client, because
        // requestedRange.getEnd() > itemCountEstimate, and it does not
        // trigger the size reset.
        verifyItemsSize(300);
    }

    @Test
    public void undefinedSize_enterClientFilter_displaysFilteredItem() {
        open(300);

        assertLoadedItemsCount("Should be 50 items before filtering", 50,
                comboBoxElement);

        // Apply text filter
        comboBoxElement.sendKeys("Callback Item 250", Keys.ENTER);

        waitForItems(comboBoxElement, items -> items.size() == 1
                && "Callback Item 250".equals(getItemLabel(items, 0)));
    }
}
