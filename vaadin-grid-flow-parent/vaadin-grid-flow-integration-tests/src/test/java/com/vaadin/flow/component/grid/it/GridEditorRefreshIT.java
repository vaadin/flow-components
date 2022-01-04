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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/editor-refresh")
public class GridEditorRefreshIT extends AbstractComponentIT {

    private GridElement grid;

    private GridColumnElement nameColumn;
    private GridColumnElement emailColumn;
    private GridTRElement row;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
        grid = $(GridElement.class).first();

        waitUntil(driver -> grid.getRowCount() > 0);

        row = grid.getRow(0);

        row.doubleClick();

        nameColumn = grid.getColumn("Name");
        emailColumn = grid.getColumn("E-mail");
    }

    @Test
    public void resetAllItems() {
        doRefresh("replace-items");
    }

    @Test
    public void refreshAllItems() {
        doRefresh("update-all");
    }

    @Test
    public void refreshItem() {
        doRefresh("update-item");
    }

    private void doRefresh(String buttonId) {
        findElement(By.id(buttonId)).click();

        assertItemIsUpdated();
        assertNoEditor();

    }

    private void assertItemIsUpdated() {
        Assert.assertEquals("bar", row.getCell(nameColumn).getText());
        Assert.assertEquals("baz@gmail.com",
                row.getCell(emailColumn).getText());
    }

    private void assertNoEditor() {
        GridTHTDElement nameCell = row.getCell(nameColumn);
        Assert.assertEquals(0, nameCell.$("vaadin-text-field").all().size());
    }
}
