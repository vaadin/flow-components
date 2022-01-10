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

import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("item-count-estimate")
public class ItemCountEstimateComboBoxIT extends AbstractItemCountComboBoxIT {

    @Test
    public void itemCountEstimate_scrollingPastEstimate_keepsScrolling() {
        int initialEstimate = 300;
        open(initialEstimate);
        verifyItemsCount(initialEstimate);

        scrollToItem(comboBoxElement, initialEstimate);

        verifyItemsCount(initialEstimate + 200);

        scrollToItem(comboBoxElement, initialEstimate + 200);

        verifyItemsCount(initialEstimate + 400);
    }

    @Test
    public void itemCountEstimate_reachesEndBeforeEstimate_itemCountChanges() {
        int initialEstimate = 500;
        open(initialEstimate);
        verifyItemsCount(initialEstimate);

        int undefinedCountBackendItemsCount = 333;
        setUnknownCountBackendItemsCount(undefinedCountBackendItemsCount);

        scrollToItem(comboBoxElement, 400);

        verifyItemsCount(undefinedCountBackendItemsCount);
        scrollToItem(comboBoxElement, undefinedCountBackendItemsCount - 1);
        waitUntilTextInContent(
                "Callback Item " + (undefinedCountBackendItemsCount - 1));

        // check that new estimate is not applied after item count is known
        setEstimate(700);

        verifyItemsCount(undefinedCountBackendItemsCount);
        scrollToItem(comboBoxElement, undefinedCountBackendItemsCount - 1);
        waitUntilTextInContent(
                "Callback Item " + (undefinedCountBackendItemsCount - 1));
    }

    @Test
    public void itemCountEstimate_switchesToDefinedCount_itemsCountChanges() {
        int initialEstimate = 500;
        open(initialEstimate);
        verifyItemsCount(initialEstimate);

        setUnknownCountBackendItemsCount(1000);
        verifyItemsCount(initialEstimate);

        setCountCallback();

        verifyItemsCount(1000);
    }

    @Test
    public void itemCountEstimate_estimateChanged_newEstimateApplied() {
        int initialEstimate = 1000;
        open(initialEstimate);
        verifyItemsCount(initialEstimate);

        setEstimate(2000);

        verifyItemsCount(2000);

        scrollToItem(comboBoxElement, 1999);
        verifyItemsCount(2200);

        setEstimate(3000);
        verifyItemsCount(3000);
    }

    @Test
    public void itemCountEstimate_estimateLessThanCurrentRange_estimateNotChanged() {
        int initialEstimate = 1000;
        open(initialEstimate);

        // "too little" estimate should not be applied
        scrollToItem(comboBoxElement, 1500);
        setEstimate(1501);

        verifyItemsCount(1501);

        setEstimate(1600);
        verifyItemsCount(1600);

        scrollToItem(comboBoxElement, 1100);
        setEstimate(1300);
        verifyItemsCount(1300);
    }

    @Test
    public void customIncrease_scrollsFarFromExactCount_countIsResolved() {
        open(3000);
        verifyItemsCount(3000);
        setUnknownCountBackendItemsCount(469);

        scrollToItem(comboBoxElement, 1000);

        verifyItemsCount(469);
    }

}
