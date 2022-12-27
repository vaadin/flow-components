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

package com.vaadin.flow.component.listbox.test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-list-box/list-box-retain-value")
public class ListBoxRetainValueIT extends AbstractComponentIT {

    private WebElement valueSpan;

    @Before
    public void init() {
        open();
        valueSpan = findElement(By.id("list-box-value"));
    }

    @Test
    public void listBoxRetainValueWhenRemovedAndAdded() {
        Assert.assertEquals("2", valueSpan.getText());
        findElement(By.id("add-button")).click();
        Assert.assertEquals("2", valueSpan.getText());
    }

    @Test
    public void selectItem_refreshAll_itemIsSelected() {
        Assert.assertEquals("2", valueSpan.getText());
        findElement(By.id("refresh-all-items")).click();
        Assert.assertEquals("2", valueSpan.getText());
    }

    @Test
    public void selectItem_removeItemFromDataSource_refreshAll_itemIsNotSelected() {
        Assert.assertEquals("2", valueSpan.getText());
        findElement(By.id("update-items")).click();
        Assert.assertEquals("", valueSpan.getText());
    }

    @Test
    public void selectItem_setItemLabelGenerator_itemIsSelected() {
        Assert.assertEquals("2", valueSpan.getText());
        findElement(By.id("update-labels")).click();
        Assert.assertEquals("2", valueSpan.getText());
    }
}
