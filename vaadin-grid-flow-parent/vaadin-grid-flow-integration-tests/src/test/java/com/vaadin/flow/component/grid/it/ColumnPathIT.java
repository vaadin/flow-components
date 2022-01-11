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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/column-path")
public class ColumnPathIT extends AbstractComponentIT {

    @Test
    public void columnsUsePathPropertyWhenApplicable() {
        open();

        GridElement grid = $(GridElement.class).waitForFirst();

        Assert.assertEquals("Person 1", grid.getCell(0, 0).getInnerHTML());
        Assert.assertEquals("Person 1", grid.getCell(0, 1).getInnerHTML());

        // A column with an editor contains lot's of stuff, so let's just check
        // if the innerHTML contains Person 1
        Assert.assertTrue(
                grid.getCell(0, 2).getInnerHTML().contains("Person 1"));

        List<WebElement> columns = grid
                .findElements(By.tagName("vaadin-grid-column"));
        Assert.assertNotNull(
                "The path property shouldn't be undefined for column 0",
                getPath(columns.get(0)));

        Assert.assertNull("The path property should be undefined for column 1",
                getPath(columns.get(1)));

        Assert.assertNull("The path property should be undefined for column 2",
                getPath(columns.get(2)));
    }

    private String getPath(WebElement col) {
        return (String) executeScript("return arguments[0].path", col);
    }

}
