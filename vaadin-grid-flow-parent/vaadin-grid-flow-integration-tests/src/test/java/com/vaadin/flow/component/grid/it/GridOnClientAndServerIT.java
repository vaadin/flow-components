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
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/grid-on-client-and-slot")
public class GridOnClientAndServerIT extends AbstractComponentIT {

    @Test
    public void treeGridOnClientShouldWorkIfAnotherGridIsAddedFromServer() {
        open();

        TestBenchElement parent = $("grid-on-client-and-slot").first();

        TreeGridElement treeGrid = parent.$(TreeGridElement.class).id("tree");
        treeGrid.getExpandToggleElement(0, 0).click();
        GridTHTDElement cell = treeGrid.getCell(1, 0);

        Assert.assertEquals("child 1-1", cell.getText().trim());

        findElement(By.id("add-new-grid-button")).click();
        treeGrid.getExpandToggleElement(3, 0).click();
        cell = treeGrid.getCell(4, 0);

        Assert.assertEquals("child 2-1", cell.getText().trim());
    }
}
