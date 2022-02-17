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
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/detailsGrid")
public class DetailsGridIT extends AbstractComponentIT {

    @Test
    public void openingMultipleDetails_navigatingAwayDoesntThrow() {
        open();

        GridElement grid = $(GridElement.class).first();

        assertAmountOfOpenDetails(grid, 0);

        clickElementWithJs(getRow(grid, 0).findElement(By.tagName("td")));

        WebElement detailsElement = grid
                .findElement(By.tagName("flow-component-renderer"));

        List<WebElement> children = detailsElement
                .findElements(By.tagName("span"));
        Assert.assertEquals(1, children.size());

        Assert.assertEquals("span",
                children.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("Jorma", children.get(0).getText());

        clickElementWithJs(getRow(grid, 1).findElement(By.tagName("td")));

        Assert.assertEquals("Expected element to be reused in new details view",
                "Jarmo", children.get(0).getText());

        clickElementWithJs(getRow(grid, 2).findElement(By.tagName("td")));

        Assert.assertEquals("Expected element to be reused in new details view",
                "Jethro", children.get(0).getText());

        findElement(By.id("next")).click();

        Assert.assertEquals(
                "Expected to find one grid on new page. Encountered something unexpected.",
                1, $("vaadin-grid").all().size());
    }

    private void assertAmountOfOpenDetails(WebElement grid,
            int expectedAmount) {
        waitUntil(driver -> grid.findElements(By.className("custom-details"))
                .size() == expectedAmount);
        Assert.assertEquals(expectedAmount,
                grid.findElements(By.className("custom-details")).size());
    }

    private WebElement getRow(GridElement grid, int row) {
        return grid.$("*").id("items").findElements(By.cssSelector("tr"))
                .get(row);
    }
}
