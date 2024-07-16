/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
