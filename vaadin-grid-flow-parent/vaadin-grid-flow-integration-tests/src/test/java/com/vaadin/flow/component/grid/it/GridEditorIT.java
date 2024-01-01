/*
 * Copyright 2000-2023 Vaadin Ltd.
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
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/editor")
public class GridEditorIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
        grid = $(GridElement.class).first();

        waitUntil(driver -> grid.getRowCount() > 0);
    }

    @Test
    public void subsequentEditRowRequested_correctRowEdited() {
        findElement(By.id("subsequent-edit-requests")).click();

        assertEditorOpenedOnRow(1);
    }

    private void assertEditorOpenedOnRow(int rowIndex) {
        final GridTHTDElement nameCell = getNameCellForRow(rowIndex);
        final ElementQuery<TestBenchElement> editor = nameCell.$("vaadin-text-field");
        Assert.assertTrue(editor.exists());
    }

    private GridTHTDElement getNameCellForRow(int rowIndex) {
        GridTRElement row = grid.getRow(rowIndex);
        return row.getCell(grid.getColumn("Name"));
    }

}
