/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/clear-items")
public class ClearItemsIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
    }

    @Test
    public void loadItems_setEmptyDataSet_open_loadingStateResolved() {
        combo.openPopup();
        assertRendered("foo");
        combo.closePopup();
        clickButton("set-empty-data-provider");
        combo.openPopup();
        assertLoadingStateResolved(combo);
        assertLoadedItemsCount("Expected no items to be loaded", 0, combo);
    }

    @Test
    public void loadItems_clearAndRefreshDataProvider_open_loadingStateResolved() {
        combo.openPopup();
        assertRendered("foo");
        combo.closePopup();
        clickButton("clear-and-refresh-data-provider");
        combo.openPopup();
        assertLoadingStateResolved(combo);
        assertLoadedItemsCount("Expected no items to be loaded", 0, combo);
    }

}
