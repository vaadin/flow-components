package com.vaadin.flow.component.select.test;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Test;

/**
 * Tests for selecting items.
 */
@TestPath("")
public class SelectionIT extends AbstractSelectIT {

    @Test
    public void testSelection_userSelection_firesValueChangeEvent() {
        for (int i = 0; i < getInitialNumberOfItems(); i++) {
            selectElement.selectItemByIndex(i);
            verify.valueChangeEvent("Item-" + i, i == 0 ? "null" : "Item-" + (i - 1), true, i);
            verify.selectedItem("Item-" + i);
        }
    }

    @Test
    public void testSelection_serverSideSelection_updatesValue() {
        page.clickSelectFirstItem();
        verify.valueChangeEvent("Item-0", "null", false, 0);
        verify.selectedItem("Item-0");

        page.clickSelectThirdItem();
        verify.valueChangeEvent("Item-2", "Item-0", false, 1);
        verify.selectedItem("Item-2");

        page.clickSelectLastItem();
        int lastItemIndex = getInitialNumberOfItems() - 1;
        verify.valueChangeEvent("Item-" + lastItemIndex, "Item-2", false, 2);
        verify.selectedItem("Item-" + lastItemIndex);
    }

    @Test
    public void testSelection_initialSelection_valueSelected() {
        openWithExtraParameter("select=0");

        verify.selectedItem("Item-0");

        openWithExtraParameter("select=3");

        verify.selectedItem("Item-3");

    }

    @Test
    public void testSelection_initialSelection_userCanChangeValue() {
        openWithExtraParameter("select=0");

        selectElement.selectItemByIndex(2);
        verify.valueChangeEvent("Item-2", "Item-0", true, 0);
        verify.selectedItem("Item-2");
    }

    @Test
    public void testSelection_initialSelection_serverCanChangeValue() {
        openWithExtraParameter("select=0");

        page.clickSelectThirdItem();
        verify.valueChangeEvent("Item-2", "Item-0", false, 0);
        verify.selectedItem("Item-2");
    }

    @Override
    protected int getInitialNumberOfItems() {
        return 5;
    }
}
