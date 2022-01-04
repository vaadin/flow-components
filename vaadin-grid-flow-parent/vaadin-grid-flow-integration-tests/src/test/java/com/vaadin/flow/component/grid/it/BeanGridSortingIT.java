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
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/bean-grid-sorting")
public class BeanGridSortingIT extends AbstractComponentIT {

    @Test
    public void sortBornColumn_valuesAreSortedAsIntegers() {
        open();
        GridElement grid = $(GridElement.class).first();

        // Original values
        Assert.assertEquals("99", grid.getCell(0, 1).getText());
        Assert.assertEquals("1111", grid.getCell(1, 1).getText());
        Assert.assertEquals("1", grid.getCell(2, 1).getText());

        // Sort by ascending order
        grid.getHeaderCell(1).$("vaadin-grid-sorter").first().click();

        Assert.assertEquals("1", grid.getCell(0, 1).getText());
        Assert.assertEquals("99", grid.getCell(1, 1).getText());
        Assert.assertEquals("1111", grid.getCell(2, 1).getText());

        // Sort by descending order
        grid.getHeaderCell(1).$("vaadin-grid-sorter").first().click();

        Assert.assertEquals("1111", grid.getCell(0, 1).getText());
        Assert.assertEquals("99", grid.getCell(1, 1).getText());
        Assert.assertEquals("1", grid.getCell(2, 1).getText());
    }

}
