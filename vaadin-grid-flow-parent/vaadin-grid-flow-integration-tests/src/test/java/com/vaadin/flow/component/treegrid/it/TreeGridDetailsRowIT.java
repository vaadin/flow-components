/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/treegrid-details-row")
public class TreeGridDetailsRowIT extends AbstractComponentIT {

    @Test
    public void gridRootItemDetailsDisplayedWhenOpen() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();

        waitUntil(e -> getDetailsElements(treegrid).size() == 1, 1);

        // each detail contain a button
        Assert.assertEquals("parent1",
                getDetailsElements(treegrid).get(0).getText());
    }

    @Test
    public void gridChildItemDetailsDisplayedWhenClicked() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();
        treegrid.expandWithClick(0);
        treegrid.getCell(2, 0).click();
        waitUntil(e -> getDetailsElements(treegrid).size() == 1, 1);

        // detail on row 1 contains a button
        Assert.assertEquals("parent1-child2",
                getDetailsElements(treegrid).get(0).getText());
    }

    @Test
    public void gridChildItemDetailsDisplayedAfterCollapseWhenClicked() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();
        treegrid.expandWithClick(0);
        treegrid.collapseWithClick(0);
        treegrid.expandWithClick(0);
        treegrid.getCell(2, 0).click();
        waitUntil(e -> getDetailsElements(treegrid).size() == 1, 1);

        // detail on row 1 contains a button
        Assert.assertEquals("parent1-child2",
                getDetailsElements(treegrid).get(0).getText());
    }

    @Test
    public void gridChildItemDetailsDisplayedAfterCollapse2WhenClicked() {
        open();
        TreeGridElement treegrid = $(TreeGridElement.class).first();
        treegrid.expandWithClick(1);
        treegrid.collapseWithClick(1);
        treegrid.expandWithClick(1);
        treegrid.getCell(2, 0).click();
        waitUntil(e -> getDetailsElements(treegrid).size() == 1, 1);

        // detail on row 1 contains a button
        Assert.assertEquals("parent2-child2",
                getDetailsElements(treegrid).get(0).getText());
    }

    private List<WebElement> getDetailsElements(TreeGridElement grid) {
        return grid.findElements(By.tagName("vaadin-button"));
    }
}
