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
package com.vaadin.flow.component.grid.contextmenu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-loads-items")
public class GridLoadsItemsIT extends AbstractComponentIT {

    @Test
    public void initialRender_twoQueries() {
        open();

        GridElement grid = $(GridElement.class).id("data-grid");
        // Even though grid is already as row 0, this makes the grid to wait
        // until it has finished loading.
        grid.scrollToRow(0);

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be just one query, that fills up the pageSize of the Grid",
                Arrays.asList("Fetch 0 - 100"), messages);
    }

    @Test
    public void scrollToPosition_oneQuery() {
        open();

        findElement(By.id("clear-messages")).click();

        GridElement grid = $(GridElement.class).id("data-grid");

        grid.scrollToRow(550);

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be one query (500 - 600), which covers the index the user is currently at",
                Arrays.asList("Fetch 500 - 600"), messages);
    }

    @Test
    public void scrollToPosition_inSteps_oneQuery() {
        open();

        findElement(By.id("clear-messages")).click();

        GridElement grid = $(GridElement.class).id("data-grid");

        // grid.scrollToRow includes logic that waits until grid has finished
        // loading items
        // so we need to use the vaadin-grid's scrollToIndex API instead (until
        // last index).
        grid.callFunction("scrollToIndex", 100);
        grid.callFunction("scrollToIndex", 200);
        grid.callFunction("scrollToIndex", 300);
        grid.callFunction("scrollToIndex", 400);
        grid.callFunction("scrollToIndex", 500);
        grid.scrollToRow(550);

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be one query (500 - 600), which covers the index the user is currently at",
                Arrays.asList("Fetch 500 - 600"), messages);
    }

    private List<String> getMessages() {
        return findElements(By.cssSelector("#messages > span")).stream()
                .map(WebElement::getText).collect(Collectors.toList());
    }

}
