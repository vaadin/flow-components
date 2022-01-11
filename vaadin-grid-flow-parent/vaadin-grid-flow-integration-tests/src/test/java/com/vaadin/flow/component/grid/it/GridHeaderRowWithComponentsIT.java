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
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-header-row-with-components")
public class GridHeaderRowWithComponentsIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid");
    }

    @Test // for https://github.com/vaadin/vaadin-grid-flow/issues/172
    public void appendHeaderRowsWithComponents_headerCellsAreRenderedInCorrectOrder() {
        List<WebElement> headerCells = getHeaderCells();
        Assert.assertEquals(
                "There should be 2 header cells after appending 2 header rows for "
                        + "a Grid with one column",
                2, headerCells.size());

        Assert.assertThat(
                "The first header cell should contain the component "
                        + "of the first appended header row",
                headerCells.get(0).getAttribute("innerHTML"),
                CoreMatchers.containsString("<label>foo</label>"));

        Assert.assertThat(
                "The second header cell should contain the component "
                        + "of the second appended header row",
                headerCells.get(1).getAttribute("innerHTML"),
                CoreMatchers.containsString("<label>bar</label>"));
    }

    @Test
    public void prependHeader_setText_setComponent_componentOverridesText() {
        findElement(By.id("set-both-text-and-component")).click();
        String headerContent = getHeaderCells().get(0)
                .getAttribute("innerHTML");

        Assert.assertThat(
                "The header cell should not contain the text after "
                        + "overriding it with a component",
                headerContent,
                CoreMatchers.not(CoreMatchers.containsString("this is text")));
        Assert.assertThat(
                "The header cell should contain the component which was last set",
                headerContent, CoreMatchers
                        .containsString("<label>this is component</label>"));
    }

    private List<WebElement> getHeaderCells() {
        WebElement thead = grid.$("*").id("header");
        List<WebElement> headers = thead.findElements(By.tagName("th"));

        List<String> cellNames = headers.stream().map(header -> header
                .findElement(By.tagName("slot")).getAttribute("name"))
                .collect(Collectors.toList());

        List<WebElement> headerCells = cellNames.stream()
                .map(name -> grid.findElement(By.cssSelector(
                        "vaadin-grid-cell-content[slot='" + name + "']")))
                .collect(Collectors.toList());

        return headerCells;
    }

}
