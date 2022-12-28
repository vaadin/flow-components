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

package com.vaadin.flow.component.select.tests;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-select/retain-value-on-refresh")
public class RetainValueOnRefreshIT extends AbstractComponentIT {

    private SelectElement select;

    @Before
    public void init() {
        open();
        select = $(SelectElement.class).first();
    }

    @Test
    public void selectItem_refreshAll_itemIsSelected() {
        select.selectByText("foo");

        Assert.assertEquals("foo", select.getSelectedOptionItem().getText());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());

        findElement(By.id("refresh-all-items")).click();

        Assert.assertEquals("foo", select.getSelectedOptionItem().getText());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());
    }

    @Test
    public void selectItem_removeItemFromDataSource_refreshAll_itemIsNotSelected() {
        select.selectByText("foo");

        Assert.assertEquals("foo", select.getSelectedOptionItem().getText());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());

        findElement(By.id("update-items")).click();

        Assert.assertTrue(select.getItemsStream()
                .noneMatch(element -> element.hasAttribute("selected")));
        Assert.assertEquals("", findElement(By.id("value-div")).getText());
    }

    @Test
    public void selectItem_setItemLabelGenerator_itemIsSelected() {
        select.selectByText("foo");

        Assert.assertEquals("foo", select.getSelectedOptionItem().getText());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());

        findElement(By.id("update-labels")).click();

        Assert.assertEquals("foo (Updated)",
                select.getSelectedOptionItem().getText());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());
    }
}
