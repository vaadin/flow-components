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
