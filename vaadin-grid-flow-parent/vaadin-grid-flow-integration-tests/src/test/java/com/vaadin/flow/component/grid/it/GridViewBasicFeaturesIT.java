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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/basic-features")
public class GridViewBasicFeaturesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void basicFeatures() {
        GridElement grid = $(GridElement.class).id("grid-basic-feature");
        scrollToElement(grid);
        waitUntil(driver -> grid.getAllColumns().size() == 11);

        TestBenchElement filteringField = grid
                .findElement(By.tagName("vaadin-text-field"));
        filteringField.sendKeys("sek");
        blur();

        Assert.assertTrue(
                "The first company name should contain the applied filter string",
                grid.getCell(0, 0).getInnerHTML().toLowerCase()
                        .contains("sek"));
    }
}
