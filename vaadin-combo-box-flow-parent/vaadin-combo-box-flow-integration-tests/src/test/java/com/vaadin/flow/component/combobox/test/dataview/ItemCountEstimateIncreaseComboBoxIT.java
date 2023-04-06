
package com.vaadin.flow.component.combobox.test.dataview;

import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("item-count-estimate-increase")
public class ItemCountEstimateIncreaseComboBoxIT
        extends AbstractItemCountComboBoxIT {

    @Test
    public void customIncrease_scrollingPastEstimate_estimateIncreased() {
        int customIncrease = 333;
        open(customIncrease);

        verifyItemsCount(getDefaultInitialItemCount());

        scrollToItem(comboBoxElement, 190);

        int newCount = getDefaultInitialItemCount() + customIncrease;
        verifyItemsCount(newCount);

        customIncrease = 500;
        setEstimateIncrease(customIncrease);

        scrollToItem(comboBoxElement, newCount - 10);

        verifyItemsCount(newCount + customIncrease);
    }

    @Test
    public void customIncrease_reachesEndBeforeEstimate_itemCountChanges() {
        open(300);

        verifyItemsCount(200);

        scrollToItem(comboBoxElement, 190);

        verifyItemsCount(500);

        setUnknownCountBackendItemsCount(469);

        scrollToItem(comboBoxElement, 444);

        verifyItemsCount(469);
    }

    @Test
    public void customIncrease_scrollsFarFromExactCount_countIsResolved() {
        open(3000);
        setUnknownCountBackendItemsCount(469);
        scrollToItem(comboBoxElement, 200);
        verifyItemsCount(3200);

        scrollToItem(comboBoxElement, 1000);

        verifyItemsCount(469);
    }

    @Test
    public void customIncreaseScrolledToEnd_newIncreaseSet_newEstimateCountNotApplied() {
        open(300);
        int unknownCountBackendItemsCount = 444;
        setUnknownCountBackendItemsCount(unknownCountBackendItemsCount);
        verifyItemsCount(200);

        scrollToItem(comboBoxElement, 190); // trigger item count bump
        scrollToItem(comboBoxElement, 500);

        verifyItemsCount(unknownCountBackendItemsCount);

        // Open the combo box drop down and scroll again to last item
        scrollToItem(comboBoxElement, unknownCountBackendItemsCount - 1);
        waitUntilTextInContent(
                "Callback Item " + (unknownCountBackendItemsCount - 1));

        // since the end was reached, only a reset() to data provider will reset
        // estimated count
        setEstimateIncrease(600);
        verifyItemsCount(unknownCountBackendItemsCount);
        // Open the combo box drop down and scroll again to last item
        scrollToItem(comboBoxElement, unknownCountBackendItemsCount - 1);
        waitUntilTextInContent(
                "Callback Item " + (unknownCountBackendItemsCount - 1));
    }

}
