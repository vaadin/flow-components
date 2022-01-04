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

import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridColumnElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/dynamic-editor-keyboard")
public class DynamicEditorKBNavigationIT extends AbstractComponentIT {

    @Test

    public void navigateBetweenEditorsUsingKeybaord() {
        open();

        GridElement grid = $(GridElement.class).first();
        waitUntil(driver -> grid.getRowCount() > 0);

        GridTRElement row = grid.getRow(0);

        GridColumnElement nameColumn = grid.getColumn("Name");
        GridTHTDElement nameCell = row.getCell(nameColumn);

        GridColumnElement subscriberColumn = grid.getAllColumns().get(1);

        GridTHTDElement subscriberCell = row.getCell(subscriberColumn);

        row.doubleClick();

        TestBenchElement nameField = nameCell.$("vaadin-text-field").first();

        TestBenchElement nameInput = nameField.$("input").first();
        nameInput.sendKeys("baz");
        nameInput.sendKeys(Keys.ENTER);
        nameInput.click();
        nameInput.sendKeys(Keys.TAB);
        TestBenchElement checkbox = subscriberCell.$("vaadin-checkbox").first();
        TestBenchElement nativeCheckbox = checkbox.$("input").first();
        nativeCheckbox.sendKeys(Keys.TAB);

        new Actions(getDriver()).sendKeys(Keys.ARROW_RIGHT, Keys.BACK_SPACE,
                Keys.BACK_SPACE, Keys.BACK_SPACE).sendKeys("org").build()
                .perform();

        grid.getRow(0).click(2, 2);

        GridTHTDElement emailCell = row.getCell(grid.getAllColumns().get(2));

        // The edited person should have new data
        WebElement msg = findElement(By.id("updated-person"));
        Assert.assertEquals("foobaz, true, bar@gmail.org", msg.getText());

        // Change the subscriber status
        checkbox.click();

        TestBenchElement emailField = emailCell.$("vaadin-text-field").first();
        String isReadonly = emailField.$("input").first()
                .getAttribute("readonly");

        Assert.assertEquals(Boolean.TRUE.toString(), isReadonly);

        nameField = nameCell.$("vaadin-text-field").first();
        nameInput.click();
        nameInput.sendKeys(Keys.TAB);
        nativeCheckbox.sendKeys(Keys.TAB);
        new Actions(getDriver()).sendKeys(Keys.BACK_SPACE).build().perform();

        emailField = emailCell.$("vaadin-text-field").first();
        Assert.assertNotNull(emailField.getAttribute("focused"));
        Assert.assertEquals("Not a subscriber",
                emailField.getAttribute("value"));

        // Change the subscriber status back
        checkbox = subscriberCell.$("vaadin-checkbox").first();
        checkbox.click();

        // Navigate using keyboard and change values in editors
        nameInput.sendKeys(Keys.BACK_SPACE, Keys.ENTER);
        nameInput.click();
        nameInput.sendKeys(Keys.TAB);

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();

        new Actions(getDriver()).sendKeys("bar.example.com").build().perform();

        grid.getRow(0).click(2, 2);

        // Check that the new values are committed
        Assert.assertEquals("fooba, true, bar.example.com", msg.getText());
    }
}
