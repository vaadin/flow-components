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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
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

    @Test
    public void editItemOutsideActiveRange_rowEdited() {
        clickElementWithJs("add-100-items");
        clickElementWithJs("edit-last-item");

        var lastRowIndex = grid.getRowCount() - 1;

        // Check that the editor is opened on the last row
        grid.scrollToRow(lastRowIndex);
        assertEditorOpenedOnRow(lastRowIndex);

        // Update the item name
        getEditor(lastRowIndex).setValue("Updated name");

        // Close the editor to save the changes
        grid.getCell(lastRowIndex - 1, 0).click();
        Assert.assertNull(getEditor(lastRowIndex));

        // Scroll back to the first row
        grid.scrollToRow(0);

        // Scroll back to the last row
        grid.scrollToRow(lastRowIndex);

        // Check that the item name is updated
        Assert.assertEquals("Updated name",
                grid.getCell(lastRowIndex, 0).getText());
    }

    private TextFieldElement getEditor(int rowIndex) {
        final GridTHTDElement nameCell = getNameCellForRow(rowIndex);
        final ElementQuery<TextFieldElement> editor = nameCell
                .$(TextFieldElement.class);
        return editor.exists() ? editor.first() : null;
    }

    private void assertEditorOpenedOnRow(int rowIndex) {
        var editor = getEditor(rowIndex);
        Assert.assertNotNull(editor);
        Assert.assertTrue(editor.isDisplayed());
    }

    private GridTHTDElement getNameCellForRow(int rowIndex) {
        GridTRElement row = grid.getRow(rowIndex);
        return row.getCell(grid.getColumn("Name"));
    }

}
