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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-checkbox/data-provider-id")
public class DataProviderIdPageIT extends AbstractComponentIT {

    @Test
    public void selectById_itemsAreNotEqualButHasSameId_itemIsSelected() {
        open();

        findElement(By.id("select-by-id")).click();

        TestBenchElement barCheckboxGroup = $("vaadin-checkbox-group")
                .id("id-data-provider").$("vaadin-checkbox").all().get(1);
        String isChecked = barCheckboxGroup.getAttribute("checked");
        Assert.assertEquals(Boolean.TRUE.toString(), isChecked);
    }

    @Test
    public void selectByEquals_itemsAreEqual_itemIsSelected() {
        open();

        findElement(By.id("select-by-equals")).click();

        TestBenchElement barCheckboxGroup = $("vaadin-checkbox-group")
                .id("standard-equals").$("vaadin-checkbox").all().get(1);
        String isChecked = barCheckboxGroup.getAttribute("checked");
        Assert.assertEquals(Boolean.TRUE.toString(), isChecked);
    }

    @Test
    public void selectById_itemsAreNotEqualButHasSameId_itemIsNotSelected() {
        open();

        findElement(By.id("no-selection")).click();

        TestBenchElement barCheckboxGroup = $("vaadin-checkbox-group")
                .id("id-data-provider").$("vaadin-checkbox").all().get(1);
        String isChecked = barCheckboxGroup.getAttribute("checked");
        Assert.assertNull(isChecked);
    }
}
