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
