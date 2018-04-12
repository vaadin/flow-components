/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("disabled-grid")
public class DisabledGridIT extends AbstractComponentIT {

    @Test
    public void gridIsDisabled_renderedButtonsAreDisabled() {
        open();

        WebElement message = findElement(By.id("message"));
        GridElement grid = $(GridElement.class).id("grid");
        GridTRElement row = grid.getRow(0);
        GridTHTDElement cell = row.getCell(grid.getColumn("Button renderer"));
        WebElement button = cell.getContext().findElement(By.tagName("button"));

        Assert.assertTrue("The rendered button should be enabled",
                button.isEnabled());

        WebElement toggleEnabled = findElement(By.id("toggleEnabled"));
        toggleEnabled.click();

        row = grid.getRow(0);
        cell = row.getCell(grid.getColumn("Button renderer"));
        button = cell.getContext().findElement(By.tagName("button"));
        Assert.assertFalse("The rendered button should be disabled",
                button.isEnabled());

        button = cell.getContext().findElement(By.tagName("button"));
        executeScript("arguments[0].disabled = false", button);
        button.click();

        Assert.assertTrue("The message should be empty",
                message.getText().isEmpty());
    }

}
