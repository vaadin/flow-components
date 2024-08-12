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
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/editor-vertical-scrolling")
public class EditorVerticalScrollingIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("editor-grid"));
        grid = $(GridElement.class).id("editor-grid");

        waitForElementPresent(By.id("edit-1"));
    }

    @Test
    public void editRow_scrollingIsDisabled_closeEditor_scrollingIsRestored() {
        String overflow = grid.$("*").id("table").getCssValue("overflow-y");
        Assert.assertEquals("auto", overflow);

        TestBenchElement editButton = grid.findElement(By.id("edit-1"));
        editButton.click();

        waitForElementPresent(By.id("cancel-1"));

        overflow = grid.$("*").id("table").getCssValue("overflow-y");
        Assert.assertEquals("hidden", overflow);

        TestBenchElement cancelButton = grid.findElement(By.id("cancel-1"));
        cancelButton.click();

        waitForElementPresent(By.id("edit-1"));

        overflow = grid.$("*").id("table").getCssValue("overflow-y");
        Assert.assertEquals("auto", overflow);
    }

}
