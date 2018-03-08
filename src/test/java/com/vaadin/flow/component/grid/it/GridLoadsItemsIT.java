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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-loads-items")
public class GridLoadsItemsIT extends AbstractComponentIT {

    @Test
    public void initialRender_twoQueries() {
        open();

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be two queries, one for eagerly fetched data and one to fill the buffer based on the size of the Grid",
                messages, Arrays.asList("Fetch 0 - 50", "Fetch 50 - 150"));
    }

    @Test
    public void scrollToPosition_oneQuery() {
        open();

        findElement(By.id("clear-messages")).click();

        WebElement grid = findElement(By.id("data-grid"));
        executeScript("return arguments[0].scrollToIndex(500)", grid);

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be one query fetching two previous pages, the current page, and two upcoming pages",
                Arrays.asList("Fetch 400 - 700"), messages);
    }

    private List<String> getMessages() {
        return findElements(By.cssSelector("#messages > span")).stream()
                .map(WebElement::getText).collect(Collectors.toList());
    }

}
