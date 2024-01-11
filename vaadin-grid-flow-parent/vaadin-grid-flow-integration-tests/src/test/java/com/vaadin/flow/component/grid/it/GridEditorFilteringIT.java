/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

@TestPath("vaadin-grid/grid-editor-filtering")
public class GridEditorFilteringIT extends AbstractComponentIT {

    private GridElement grid;

    private TextFieldElement nameFilter;

    private ButtonElement searchButton;

    private ButtonElement addColumn;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        nameFilter = $(TextFieldElement.class).id("name-filter");
        searchButton = $(ButtonElement.class).id("search-button");
        addColumn = $(ButtonElement.class).id("add-column");
    }

    @Test
    public void scrollToAnotherPage_clickCell_filterItems_addColumn_editorStillOpen() {
        int indexToScrollTo = 98;

        grid.scrollToRow(indexToScrollTo);

        GridTRElement row = grid.getRow(indexToScrollTo);

        GridColumnElement firstNameColumn = grid.getColumn("First Name");
        GridTHTDElement firstNameCell = row.getCell(firstNameColumn);

        firstNameCell.focus();
        firstNameCell.click();

        waitUntil(__ -> firstNameCell.$(TextFieldElement.class).exists());

        nameFilter.focus();
        nameFilter.sendKeys("Name 9");
        nameFilter.sendKeys(Keys.ENTER);

        searchButton.click();

        int updatedIndex = 9;
        waitUntil(__ -> grid.getRow(updatedIndex).getCell(firstNameColumn)
                .$(TextFieldElement.class).exists());

        addColumn.click();

        waitUntil(__ -> grid.getRow(updatedIndex).getCell(firstNameColumn)
                .$(TextFieldElement.class).exists());
    }
}
