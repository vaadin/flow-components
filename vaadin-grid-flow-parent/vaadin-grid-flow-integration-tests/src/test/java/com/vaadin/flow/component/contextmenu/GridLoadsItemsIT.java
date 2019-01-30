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
package com.vaadin.flow.component.contextmenu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-loads-items")
public class GridLoadsItemsIT extends AbstractComponentIT {

    @Test
    public void initialRender_twoQueries() {
        open();

        // waits for Grid to fetch the items after it is loaded. This process is
        // asynchronous - without this wait, the test might fail.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be just one query, that fills up the pageSize of the Grid",
                Arrays.asList("Fetch 0 - 50"), messages);
    }

    @Test
    public void scrollToPosition_oneQuery() {
        open();

        findElement(By.id("clear-messages")).click();

        GridElement grid = $(GridElement.class).id("data-grid");
        grid.scrollToRow(500);

        List<String> messages = getMessages();

        Assert.assertEquals(
                "There should be one query fetching two previous pages (400-450 + 450-500), the current page (500-550), and one upcoming page (550-600)",
                Arrays.asList("Fetch 400 - 600"), messages);
    }

    private List<String> getMessages() {
        return findElements(By.cssSelector("#messages > span")).stream()
                .map(WebElement::getText).collect(Collectors.toList());
    }

}
