

package com.vaadin.flow.component.grid.dataview;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-grid/item-count-estimate-increase")
public class ItemCountEstimateIncreaseGridIT extends AbstractItemCountGridIT {

    @Test
    public void customIncrease_scrollingPastEstimate_estimateIncreased() {
        int customIncrease = 333;
        open(customIncrease);

        verifyRows(getDefaultInitialItemCount());

        grid.scrollToRow(190);

        int newCount = getDefaultInitialItemCount() + customIncrease;
        verifyRows(newCount);

        customIncrease = 500;
        setEstimateIncrease(customIncrease);

        grid.scrollToRow(newCount - 10);

        verifyRows(newCount + customIncrease);
    }

    @Test
    public void customIncrease_reachesEndBeforeEstimate_sizeChanges() {
        open(300);

        verifyRows(200);

        grid.scrollToRow(190);

        verifyRows(500);

        setUnknownCountBackendSize(469);

        grid.scrollToRow(444);

        verifyRows(469);
    }

    @Test
    public void customIncrease_scrollsFarFromExactCount_countIsResolved() {
        open(3000);
        setUnknownCountBackendSize(469);
        grid.scrollToRow(200);
        verifyRows(3200);

        grid.scrollToRow(1000);

        verifyRows(469);
    }

    @Test
    public void customIncreaseScrolledToEnd_newIncreaseSet_newEstimateSizeNotApplied() {
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
        setEstimateIncrease(600);
        verifyRows(unknownCountBackendSize);
        Assert.assertEquals("Last visible row wrong",
                unknownCountBackendSize - 1, grid.getLastVisibleRowIndex());
    }

}
