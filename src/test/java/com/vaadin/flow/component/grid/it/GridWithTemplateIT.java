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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-template-test")
public class GridWithTemplateIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("grid-in-a-template"));
        waitForElementPresent(By.tagName("vaadin-grid"));
    }

    @Test
    public void injectedGrid_cellWithTemplates_buttonIsClicked_cellIsUpdated() {
        WebElement gridInATemplate = findElement(
                By.id("injected-template-in-cells"));
        WebElement grid = findInShadowRoot(gridInATemplate,
                By.id("injected-template-in-cells")).get(0);
        scrollToElement(gridInATemplate);

        for (int i = 1; i <= 3; i++) {
            clickOnTheButtonInsideTheTestTemplate(grid,
                    "injected-template-in-cells-item-" + i, 3);
        }
    }

    @Test
    public void standaloneGrid_cellWithTemplates_buttonIsClicked_cellIsUpdated() {
        WebElement grid = findElement(By.id("standalone-template-in-cells"));
        scrollToElement(grid);

        for (int i = 1; i <= 3; i++) {
            clickOnTheButtonInsideTheTestTemplate(grid,
                    "standalone-template-in-cells-item-" + i, 3);
        }
    }

    @Test
    public void injectedGrid_headerWithTemplates_buttonIsClicked_headerIsUpdated() {
        WebElement gridInATemplate = findElement(
                By.id("injected-template-in-header"));
        WebElement grid = findInShadowRoot(gridInATemplate,
                By.id("injected-template-in-header")).get(0);
        scrollToElement(gridInATemplate);

        clickOnTheButtonInsideTheTestTemplate(grid,
                "injected-template-in-header-header", 3);
        clickOnTheButtonInsideTheTestTemplate(grid,
                "injected-template-in-header-footer", 3);
    }

    @Test
    public void standaloneGrid_headerWithTemplates_buttonIsClicked_headerIsUpdated() {
        WebElement grid = findElement(By.id("standalone-template-in-header"));
        scrollToElement(grid);

        clickOnTheButtonInsideTheTestTemplate(grid,
                "standalone-template-in-header-header", 3);
        clickOnTheButtonInsideTheTestTemplate(grid,
                "standalone-template-in-header-footer", 3);
    }

    @Test
    @Ignore
    /*
     * Ignored due to the issue
     * https://github.com/vaadin/vaadin-grid-flow/issues/71
     */
    public void injectedGrid_detailsWithTemplates_buttonIsClicked_detailIsUpdated() {
        WebElement gridInATemplate = findElement(
                By.id("injected-template-in-details"));
        WebElement grid = findInShadowRoot(gridInATemplate,
                By.id("injected-template-in-details")).get(0);
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
        WebElement grid = findElement(By.id("standalone-template-in-details"));
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

    private void clickOnTheButtonInsideTheTestTemplate(WebElement grid,
            String id, int numberOfClicks) {
        WebElement template = findTestTemplateElement(grid, id);
        WebElement container = findInShadowRoot(template, By.id("container"))
                .get(0);

        List<WebElement> spans = container.findElements(By.tagName("span"));
        Assert.assertTrue(spans.isEmpty());

        for (int i = 0; i < numberOfClicks; i++) {
            WebElement btn = findInShadowRoot(template, By.id("btn")).get(0);
            clickElementWithJs(btn);

            int size = i + 1;
            WebElement label = container.findElement(By.id("label-" + size));
            Assert.assertEquals("Label " + size, label.getText());
        }
    }

    private WebElement findTestTemplateElement(WebElement grid, String id) {
        List<WebElement> list = grid.findElements(By.id(id));
        Assert.assertFalse(
                "Could not find the <test-template> of id '" + id
                        + "' inside the grid '" + grid.getAttribute("id") + "'",
                list.isEmpty());
        return list.get(0);
    }

    private WebElement getFirstCellOfRow(WebElement grid, int row) {
        return getInShadowRoot(grid, By.id("items"))
                .findElements(By.tagName("tr")).get(row)
                .findElement(By.tagName("td"));
    }

}
