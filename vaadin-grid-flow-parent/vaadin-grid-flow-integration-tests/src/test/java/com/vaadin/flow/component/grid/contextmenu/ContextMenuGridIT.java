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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-grid/context-menu-grid")
public class ContextMenuGridIT extends AbstractComponentIT {

    private static final String OVERLAY_TAG = "vaadin-context-menu-overlay";

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).first();
        verifyClosed();
    }

    @Test
    public void contextClickOnRow_itemClickGetsTargetItem() {
        grid.getCell(56, 1).contextClick();
        $("vaadin-context-menu-item").first().click();
        assertMessage("Person 56");
        verifyClosed();
    }

    @Test
    public void contextClickOnEdgeOfCell_itemClickGetsTargetItem() {
        GridTHTDElement cell = grid.getCell(56, 0);
        // Move cursor to upper edge of the cell
        // moveToElement moves to center, so we subtract half of the height to
        // approximately get to the cells edge
        Rectangle cellRectangle = cell.getWrappedElement().getRect();
        int offsetToCellStart = (int) Math
                .ceil((float) cellRectangle.height / 2);
        (new Actions(this.getDriver()))
                .moveToElement(cell, 0, -offsetToCellStart).contextClick()
                .build().perform();
        $("vaadin-context-menu-item").first().click();
        assertMessage("Person 56");
        verifyClosed();
    }

    @Test
    public void contextClickOnRow_itemClickGetsGrid() {
        grid.getCell(56, 1).contextClick();
        $("vaadin-context-menu-item").get(1).click();
        assertMessage("Grid id: grid-with-context-menu");
        verifyClosed();
    }

    @Test
    public void contextClickOnHeader_targetItemReturnsNull() {
        grid.getHeaderCell(0).contextClick();
        $("vaadin-context-menu-item").first().click();
        assertMessage("no target item");
        verifyClosed();
    }

    @Test
    public void setOpenOnClick_clickOnRow_itemClickGetsTargetItem() {
        $("button").id("toggle-open-on-click").click();
        grid.getCell(14, 0).click();
        $("vaadin-context-menu-item").first().click();
        assertMessage("Person 14");
        verifyClosed();
    }

    @Test
    public void setOpenOnClick_contextClickOnRow_noContextMenuOpen() {
        $("button").id("toggle-open-on-click").click();
        grid.getCell(22, 0).contextClick();
        verifyClosed();
    }

    @Test
    public void addSubMenu_itemClickGetsTargetItemAndGrid() {
        $("button").id("add-sub-menu").click();
        grid.getCell(45, 1).contextClick();
        openSubMenu($("vaadin-context-menu-item").get(3));
        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 2);
        getSubMenuItems().get(0).click();
        assertMessage("Person 45");
        verifyClosed();

        grid.getCell(29, 0).contextClick();
        openSubMenu($("vaadin-context-menu-item").get(3));
        waitUntil(driver -> $("vaadin-context-menu-overlay").all().size() == 2);
        getSubMenuItems().get(1).click();
        assertMessage("Grid id: grid-with-context-menu");
        verifyClosed();
    }

    @Test
    public void gridInATemplateWithContextMenu_itemClickGetsTargetItem() {
        GridElement gridInATemplate = $("grid-in-a-template").first()
                .$(GridElement.class).first();
        gridInATemplate.getCell(18, 0).contextClick();
        $("vaadin-context-menu-item").first().click();
        assertMessage("Item 18");
    }

    @Test
    public void menuHasComponents_componentsAreNotItems() {
        GridElement grid = $(GridElement.class).id("grid-with-context-menu");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        grid.getCell(0, 0).contextClick();

        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 1);

        TestBenchElement menuOverlay = $(OVERLAY_TAG).first();

        TestBenchElement overlayContainer = menuOverlay
                .$("vaadin-context-menu-list-box").first();
        List<WebElement> items = overlayContainer.findElements(By.xpath("./*"));
        Assert.assertEquals(4, items.size());
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("hr",
                items.get(1).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(2).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(3).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("Link", items.get(3).getText());
    }

    @Test
    public void subMenuHasComponents_componentsAreNotItems() {
        GridElement grid = $(GridElement.class).id("grid-with-context-menu");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        $(TestBenchElement.class).id("add-sub-menu").click();

        grid.getCell(0, 0).contextClick();

        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 1);
        TestBenchElement menuOverlay = $(OVERLAY_TAG).first();

        TestBenchElement overlayContainer = menuOverlay
                .$("vaadin-context-menu-list-box").first();
        openSubMenu(overlayContainer.$("vaadin-context-menu-item").get(3));

        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 2);
        TestBenchElement subMenuOverlay = $(OVERLAY_TAG).get(1);

        List<WebElement> items = subMenuOverlay
                .$("vaadin-context-menu-list-box").first()
                .findElements(By.xpath("./*"));
        Assert.assertEquals(4, items.size());
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(0).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("h1",
                items.get(1).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("bar", items.get(1).getText());
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(2).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("vaadin-context-menu-item",
                items.get(3).getTagName().toLowerCase(Locale.ENGLISH));
        Assert.assertEquals("Link", items.get(3).getText());

        subMenuOverlay.$("vaadin-context-menu-item").get(0).click();

        assertMessage("Person 0");
    }

    @Test
    public void removeContextMenu_menuIsNotShown() {
        GridElement grid = $(GridElement.class).id("grid-with-context-menu");
        scrollToElement(grid);
        waitUntil(driver -> grid.getRowCount() > 0);

        grid.getCell(0, 0).contextClick();
        waitUntil(driver -> $(OVERLAY_TAG).all().size() == 1);
        $(OVERLAY_TAG).get(0).$("vaadin-context-menu-item").first().click();

        verifyClosed();

        $(TestBenchElement.class).id("remove-context-menu").click();

        grid.getCell(0, 0).contextClick();

        getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        Assert.assertFalse(isElementPresent(By.tagName(OVERLAY_TAG)));
    }

    @Test
    public void contextClickOnRow_preOpenGetsTargetItemCol0() {
        grid.getCell(23, 0).contextClick();
        assertMessage("pre-open: name=Person 23, colId=Name-Id");

        // ensure closing
        grid.getCell(29, 1).contextClick();
        verifyClosed();
    }

    @Test
    public void contextClickOnRow_preOpenGetsTargetItemCol1() {
        grid.getCell(6, 1).contextClick();
        assertMessage("pre-open: name=Person 6, colId=Born-Id");

        // ensure closing
        grid.getCell(19, 1).contextClick();
        verifyClosed();
    }

    private void assertMessage(String expected) {
        Assert.assertEquals(expected, $("label").id("message").getText());
    }

    private void openSubMenu(TestBenchElement parentItem) {
        executeScript(
                "arguments[0].dispatchEvent(new Event('mouseover', {bubbles:true}))",
                parentItem);
    }

    private List<TestBenchElement> getSubMenuItems() {
        return $(OVERLAY_TAG).get(1).$("vaadin-context-menu-item").all();
    }

    private void verifyClosed() {
        waitForElementNotPresent(By.tagName(OVERLAY_TAG));
    }

}
