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

package com.vaadin.flow.component.grid.dataview;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("vaadin-grid/item-count-callback")
public class ItemCountCallbackGridIT extends AbstractItemCountGridIT {

    @Test
    public void itemCountCallbackCallbackGrid_scrolledToMiddleAndSwitchesToUndefinedSize_canScrollPastOldKnownSize() {
        open(500);

        grid.scrollToRow(250);

        verifyRows(500);

        setUnknownCountBackendSize(1000);
        setUnknownCount();

        verifyRows(500);

        grid.scrollToRow(500);

        verifyRows(700);
    }

    @Test
    public void itemCountCallbackCallbackGrid_scrolledToEndAndSwitchesToUndefinedSize_sizeIsIncreased() {
        open(5800);

        verifyRows(5800);

        grid.scrollToRow(5800);

        Assert.assertEquals(5799, grid.getLastVisibleRowIndex());

        setUnknownCountBackendSize(10000);
        setUnknownCount();

        verifyRows(6000);

        grid.scrollToRow(6000);

        verifyRows(6200);
    }

}
