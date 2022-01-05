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

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/update-editor")
public class UpdateEditorComponentIT extends AbstractComponentIT {

    @Test
    public void updateEditorComponent() {
        open();

        GridElement grid = $(GridElement.class).first();
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        row.doubleClick();

        TestBenchElement textField = nameCell.$("vaadin-text-field").first()
                .$("input").first();
        Assert.assertEquals("foo", textField.getAttribute("value"));

        // close the editor via clicking another cell
        grid.getRow(1).click(10, 10);

        // change binding => update editor component
        findElement(By.id("update-editor")).click();

        row.doubleClick();

        // Now it should be a text area component
        TestBenchElement textArea = nameCell.$("vaadin-text-area").first()
                .$("textarea").first();
        Assert.assertEquals("foo", textArea.getAttribute("value"));
    }
}
