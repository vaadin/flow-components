/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import elemental.json.JsonType;

@TestPath("vaadin-virtual-list/inert")
public class InertVirtualListIT extends AbstractComponentIT {

    private VirtualListElement virtualList;

    @Before
    public void init() {
        open();
        var loadingIndicator = findElement(By.className("v-loading-indicator"));
        waitUntil(driver -> !loadingIndicator.isDisplayed());
        virtualList = $(VirtualListElement.class).waitForFirst();
    }

    @Test
    public void inertVirtualList_itemsAreRendered() {
        var items = VirtualListHelpers.getItems(getDriver(), virtualList);
        Assert.assertTrue(items.length() > 0);
        for (var i = 0; i < items.length(); i++) {
            Assert.assertNotEquals(JsonType.NULL, items.get(i).getType());
        }
    }
}
