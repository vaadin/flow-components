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
package com.vaadin.flow.component.checkbox.tests;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-checkbox/refresh-data-provider")
public class RefreshDataProviderPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void resetComponentOnDataProviderRefresh() {
        findElement(By.id("update-items")).click();

        List<TestBenchElement> radioButtons = $("vaadin-checkbox").all();
        Assert.assertEquals(2, radioButtons.size());

        Assert.assertEquals("bar", radioButtons.get(0).getText());
        Assert.assertEquals("baz", radioButtons.get(1).getText());
    }

    @Test
    public void resetComponentExpectLabel() {
        findElement(By.id("update-items")).click();

        TestBenchElement group = $(TestBenchElement.class).id("group");
        String label = group.findElement(By.cssSelector("label[slot='label']"))
                .getText();
        Assert.assertEquals("Label", label);
    }

    @Test
    public void selectItem_refreshAll_itemIsSelected() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class).first();
        group.selectByText("foo");
        Assert.assertEquals(Arrays.asList("foo"), group.getSelectedTexts());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());

        findElement(By.id("refresh-all-items")).click();

        Assert.assertEquals(Arrays.asList("foo"), group.getSelectedTexts());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());
    }

    @Test
    public void selectItem_removeItemFromDataSource_refreshAll_itemIsNotSelected() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class).first();

        group.selectByText("foo");
        group.selectByText("bar");
        Assert.assertEquals(Arrays.asList("foo", "bar"),
                group.getSelectedTexts());
        Assert.assertEquals("bar, foo",
                findElement(By.id("value-div")).getText());

        findElement(By.id("update-items")).click();

        Assert.assertEquals(Arrays.asList("bar"), group.getSelectedTexts());
        Assert.assertEquals("bar", findElement(By.id("value-div")).getText());
    }

    @Test
    public void selectItem_setItemLabelGenerator_itemIsSelected() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class).first();

        group.selectByText("foo");
        Assert.assertEquals(Arrays.asList("foo"), group.getSelectedTexts());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());

        findElement(By.id("update-labels")).click();

        Assert.assertEquals(Arrays.asList("foo (Updated)"),
                group.getSelectedTexts());
        Assert.assertEquals("foo", findElement(By.id("value-div")).getText());

    }
}
