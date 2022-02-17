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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/grid-template-test")
public class GridWithTemplateIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("grid-in-a-template"));
        waitForElementPresent(By.tagName("vaadin-grid"));
    }

    @Test
    public void injectedGrid_cellWithTemplates_buttonIsClicked_cellIsUpdated() {
        TestBenchElement gridInATemplate = $("*")
                .id("injected-template-in-cells");
        TestBenchElement grid = gridInATemplate.$("*")
                .id("injected-template-in-cells");
        scrollToElement(gridInATemplate);

        for (int i = 1; i <= 3; i++) {
            clickOnTheButtonInsideTheTestTemplate(grid,
                    "injected-template-in-cells-item-" + i, 3);
        }
    }

    @Test
    public void standaloneGrid_cellWithTemplates_buttonIsClicked_cellIsUpdated() {
        TestBenchElement grid = $("*").id("standalone-template-in-cells");
        scrollToElement(grid);

        for (int i = 1; i <= 3; i++) {
            clickOnTheButtonInsideTheTestTemplate(grid,
                    "standalone-template-in-cells-item-" + i, 3);
        }
    }

    @Test
    public void injectedGrid_headerWithTemplates_buttonIsClicked_headerIsUpdated() {
        TestBenchElement gridInATemplate = $("*")
                .id("injected-template-in-header");
        TestBenchElement grid = gridInATemplate.$("*")
                .id("injected-template-in-header");
        scrollToElement(gridInATemplate);

        clickOnTheButtonInsideTheTestTemplate(grid,
                "injected-template-in-header-header", 3);
        clickOnTheButtonInsideTheTestTemplate(grid,
                "injected-template-in-header-footer", 3);
    }

    @Test
    public void standaloneGrid_headerWithTemplates_buttonIsClicked_headerIsUpdated() {
        TestBenchElement grid = $("*").id("standalone-template-in-header");
        scrollToElement(grid);

        clickOnTheButtonInsideTheTestTemplate(grid,
                "standalone-template-in-header-header", 3);
        clickOnTheButtonInsideTheTestTemplate(grid,
                "standalone-template-in-header-footer", 3);
    }

    @Test
    /*
     * Test for the issue https://github.com/vaadin/vaadin-grid-flow/issues/71
     */
    public void injectedGrid_detailsWithTemplates_buttonIsClicked_detailIsUpdated() {
        TestBenchElement gridInATemplate = $("*")
                .id("injected-template-in-details");
        TestBenchElement grid = gridInATemplate.$("*")
                .id("injected-template-in-details");
        scrollToElement(gridInATemplate);

        for (int i = 1; i <= 3; i++) {
            int idx = i;
            clickElementWithJs(getFirstCellOfRow(grid, idx - 1));
            waitUntil(driver -> !grid
                    .findElements(
                            By.id("injected-template-in-details-item-" + idx))
                    .isEmpty());

            clickOnTheButtonInsideTheTestTemplate(grid,
                    "injected-template-in-details-item-" + idx, 1);
        }
    }

    @Test
    public void standaloneGrid_detailsWithTemplates_buttonIsClicked_detailIsUpdated() {
        TestBenchElement grid = $("*").id("standalone-template-in-details");
        scrollToElement(grid);

        for (int i = 1; i <= 3; i++) {
            int idx = i;
            clickElementWithJs(getFirstCellOfRow(grid, idx - 1));
            waitUntil(driver -> !grid
                    .findElements(
                            By.id("standalone-template-in-details-item-" + idx))
                    .isEmpty());

            /*
             * We only click once at this test. The reason is: when clicking
             * more than once, the click closes the details row and removes it
             * from the DOM - causing Stale Element Reference Exceptions on
             * selenium. Reselecting the elements don't fix the problem, because
             * the details row is created anew every time it is opened.
             */
            clickOnTheButtonInsideTheTestTemplate(grid,
                    "standalone-template-in-details-item-" + idx, 1);
        }
    }

    @Test
    public void injectedGrid_columnsWithProperties() {
        TestBenchElement gridInATemplate = $("*")
                .id("injected-columns-with-properties");
        WebElement grid = gridInATemplate.$("*")
                .id("injected-columns-with-properties");
        assertColumnProperties(grid);
    }

    @Test
    public void standaloneGrid_columnsWithProperties() {
        WebElement grid = findElement(
                By.id("standalone-columns-with-properties"));
        assertColumnProperties(grid);
    }

    private void assertColumnProperties(WebElement grid) {
        scrollToElement(grid);
        List<WebElement> columns = grid
                .findElements(By.tagName("vaadin-grid-column"));

        Assert.assertEquals(3, columns.size());

        Assert.assertEquals(
                "The flexGrow property should be 2 on the first column", "2",
                columns.get(0).getAttribute("flexGrow"));
        Assert.assertEquals(
                "The flexGrow property should be 0 on the second column", "0",
                columns.get(1).getAttribute("flexGrow"));
        Assert.assertEquals(
                "The width property should be 20px on the second column",
                "20px", columns.get(1).getAttribute("width"));
        Assert.assertEquals(
                "The frozen property should be true on the third column",
                "true", columns.get(2).getAttribute("frozen"));
        Assert.assertEquals(
                "The resizable property should be true on the third column",
                "true", columns.get(2).getAttribute("resizable"));
    }

    private void clickOnTheButtonInsideTheTestTemplate(TestBenchElement grid,
            String id, int numberOfClicks) {
        TestBenchElement template = findTestTemplateElement(grid, id);
        WebElement container = template.$("*").id("container");

        List<WebElement> spans = container.findElements(By.tagName("span"));
        Assert.assertTrue(spans.isEmpty());

        for (int i = 0; i < numberOfClicks; i++) {
            WebElement btn = template.$("*").id("btn");
            clickElementWithJs(btn);

            int size = i + 1;
            WebElement label = container.findElement(By.id("label-" + size));
            Assert.assertEquals("Label " + size, label.getText());
        }
    }

    private TestBenchElement findTestTemplateElement(TestBenchElement grid,
            String id) {
        TestBenchElement list = grid.$("*").id(id);
        Assert.assertNotNull(
                "Could not find the <test-template> of id '" + id
                        + "' inside the grid '" + grid.getAttribute("id") + "'",
                list);
        return list;
    }

    private WebElement getFirstCellOfRow(TestBenchElement grid, int row) {
        return grid.$("*").id("items").findElements(By.tagName("tr")).get(row)
                .findElement(By.tagName("td"));
    }

}
