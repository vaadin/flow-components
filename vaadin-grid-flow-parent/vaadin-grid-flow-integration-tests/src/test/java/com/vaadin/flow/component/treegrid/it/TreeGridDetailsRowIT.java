/*
 * Copyright 2000-2024 Vaadin Ltd.
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
