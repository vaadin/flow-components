

package com.vaadin.flow.component.combobox.test.dataview;

import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("item-count-callback")
public class ItemCountCallbackComboBoxIT extends AbstractItemCountComboBoxIT {

    @Test
    public void itemCountCallbackCallback_scrolledToMiddleAndSwitchesToUndefinedCount_canScrollPastOldKnownCount() {
        open(500);

        scrollToItem(comboBoxElement, 250);

        verifyItemsCount(500);

        setUnknownCountBackendItemsCount(1000);
        setUnknownCount();

        verifyItemsCount(500);

        scrollToItem(comboBoxElement, 500);

        verifyItemsCount(700);
    }

    @Test
    public void itemCountCallbackCallback_scrolledToEndAndSwitchesToUndefinedCount_itemCountIsIncreased() {
        open(5800);

        verifyItemsCount(5800);

        scrollToItem(comboBoxElement, 5800);
        waitUntilTextInContent("Callback Item " + 5799);

        verifyItemsCount(5800);

        setUnknownCountBackendItemsCount(10000);
        setUnknownCount();

        verifyItemsCount(6000);

        scrollToItem(comboBoxElement, 6000);

        verifyItemsCount(6200);
    }

}
