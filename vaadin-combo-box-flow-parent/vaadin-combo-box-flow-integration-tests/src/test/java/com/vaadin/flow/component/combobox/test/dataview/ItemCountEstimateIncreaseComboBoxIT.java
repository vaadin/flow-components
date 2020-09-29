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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("item-count-estimate-increase")
public class ItemCountEstimateIncreaseComboBoxIT extends AbstractItemCountComboBoxIT {

    @Test
    public void customIncrease_scrollingPastEstimate_estimateIncreased() {
        int customIncrease = 333;
        open(customIncrease);

        verifyItemsSize(getDefaultInitialItemCount());

        scrollToItem(comboBoxElement, 190);

        int newCount = getDefaultInitialItemCount() + customIncrease;
        verifyItemsSize(newCount);

        customIncrease = 500;
        setEstimateIncrease(customIncrease);

        scrollToItem(comboBoxElement, newCount - 10);

        verifyItemsSize(newCount + customIncrease);
    }

    @Test
    public void customIncrease_reachesEndBeforeEstimate_sizeChanges() {
        open(300);

        verifyItemsSize(200);

        scrollToItem(comboBoxElement, 190);

        verifyItemsSize(500);

        setUnknownCountBackendSize(469);

        scrollToItem(comboBoxElement, 444);

        verifyItemsSize(469);
    }

    @Test
    public void customIncrease_scrollsFarFromExactCount_countIsResolved() {
        open(3000);
        setUnknownCountBackendSize(469);
        scrollToItem(comboBoxElement, 200);
        verifyItemsSize(3200);

        scrollToItem(comboBoxElement, 1000);

        verifyItemsSize(469);
    }

    @Test
    public void customIncreaseScrolledToEnd_newIncreaseSet_newEstimateSizeNotApplied() {
        open(300);
        int unknownCountBackendSize = 444;
        setUnknownCountBackendSize(unknownCountBackendSize);
        verifyItemsSize(200);

        scrollToItem(comboBoxElement, 190); // trigger size bump
        scrollToItem(comboBoxElement, 500);

        verifyItemsSize(unknownCountBackendSize);

        // Open the combo box drop down and scroll again to last item
        scrollToItem(comboBoxElement, unknownCountBackendSize - 1);
        waitUntilTextInContent("Callback Item " + (unknownCountBackendSize - 1));

        // since the end was reached, only a reset() to data provider will reset
        // estimated size
        setEstimateIncrease(600);
        verifyItemsSize(unknownCountBackendSize);
        // Open the combo box drop down and scroll again to last item
        scrollToItem(comboBoxElement, unknownCountBackendSize - 1);
        waitUntilTextInContent("Callback Item " + (unknownCountBackendSize - 1));
    }

}
