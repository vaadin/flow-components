/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("grid-component-renderer")
public class GridComponentRendererIT extends AbstractComponentIT {

    private GridElement grid;
    private WebElement adRowButton;

    @Before
    public void init() {
        open();

        waitForElementPresent(By.id("add-row-button"));
        adRowButton = this.findElement(By.id("add-row-button"));

        waitForElementPresent(By.id("grid"));
        grid = $(GridElement.class).id("grid");


    }

    @Test
    public void addRowWithComponents_all_components_are_visible(){

        GridTHTDElement cell;

        for(int rowIndex = 0; rowIndex < 10; rowIndex ++){
            clickElementWithJs(adRowButton);

            cell = grid.getCell(grid.getRowCount() - 1, 0);
            Assert.assertTrue("TextField is not present!", cell.$("vaadin-text-field").exists());
            Assert.assertTrue("TextField is not displayed!", cell.$("vaadin-text-field").first().isDisplayed());

            for(int colIndex = 1; colIndex < 5; colIndex++){
                cell = grid.getCell(grid.getRowCount() - 1, colIndex);
                Assert.assertTrue("Combobox is not present!", cell.$("vaadin-combo-box").exists());
                Assert.assertTrue("Combobox is not displayed!", cell.$("vaadin-combo-box").first().isDisplayed());
            }
        }

    }
}
