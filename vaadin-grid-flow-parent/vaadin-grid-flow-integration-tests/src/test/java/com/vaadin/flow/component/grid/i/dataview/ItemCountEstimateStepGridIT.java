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

package com.vaadin.flow.component.grid.i.dataview;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("item-count-estimate-step")
public class ItemCountEstimateStepGridIT extends AbstractItemCountGridIT {

    @Test
    public void customStep_scrollingPastEstimate_estimateIncreased() {
        int customStep = 333;
        open(customStep);

        verifyRows(getDefaultInitialItemCount());

        grid.scrollToRow(190);

        int newCount = getDefaultInitialItemCount() + customStep;
        verifyRows(newCount);

        customStep = 500;
        setEstimateStep(customStep);

        grid.scrollToRow(newCount - 10);

        verifyRows(newCount + customStep);
    }

    @Test
    public void customStep_reachesEndBeforeEstimate_sizeChanges() {
        open(300);

        verifyRows(200);

        grid.scrollToRow(190);

        verifyRows(500);

        setUnknownCountBackendSize(469);

        grid.scrollToRow(444);

        verifyRows(469);
    }

    @Test
    public void customStepScrolledToEnd_newStepSet_newEstimateSizeNotApplied() {
        open(300);
        int unknownCountBackendSize = 444;
        setUnknownCountBackendSize(unknownCountBackendSize);
        verifyRows(200);

        grid.scrollToRow(190); // trigger size bump
        grid.scrollToRow(500);

        verifyRows(unknownCountBackendSize);
        Assert.assertEquals("Last visible row wrong",
                unknownCountBackendSize - 1, grid.getLastVisibleRowIndex());

        // since the end was reached, only a reset() to data provider will reset
        // estimated size
        setEstimateStep(600);
        verifyRows(unknownCountBackendSize);
        Assert.assertEquals("Last visible row wrong",
                unknownCountBackendSize - 1, grid.getLastVisibleRowIndex());
    }

}
