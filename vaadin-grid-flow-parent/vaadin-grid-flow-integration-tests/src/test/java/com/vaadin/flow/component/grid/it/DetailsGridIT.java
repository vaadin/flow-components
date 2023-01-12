/*
 * Copyright 2000-2023 Vaadin Ltd.
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

@TestPath("vaadin-grid/detailsGrid")
public class DetailsGridIT extends AbstractComponentIT {

    @Test
    public void openingMultipleDetails_navigatingAwayDoesntThrow() {
        open();

        GridElement grid = $(GridElement.class).first();

        assertAmountOfOpenDetails(grid, 0);

        grid.getCell(0, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        Assert.assertEquals("Jorma", getDetailsElements(grid).get(0).getText());

        grid.getCell(1, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        Assert.assertEquals("Expected element to be reused in new details view",
                "Jarmo", getDetailsElements(grid).get(0).getText());

        grid.getCell(2, 0).click();
        waitUntil(e -> getDetailsElements(grid).size() == 1, 1);

        Assert.assertEquals("Expected element to be reused in new details view",
                "Jethro", getDetailsElements(grid).get(0).getText());

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

    private List<WebElement> getDetailsElements(GridElement grid) {
        return grid.findElements(By.tagName("span"));
    }
}
