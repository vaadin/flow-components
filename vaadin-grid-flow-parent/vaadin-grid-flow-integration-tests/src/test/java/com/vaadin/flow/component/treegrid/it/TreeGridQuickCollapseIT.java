/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-preload")
public class TreeGridQuickCollapseIT extends AbstractTreeGridIT {

    @Test
    public void expandAndCollapseQuickly_slowDataProvider_shouldShowChildrenWhenExpandedAgain()
            throws Exception {
        open("nodesPerLevel=100&pageSize=25&responseDelay=1000");
        setupTreeGrid();

        // quick expand and collapse
        WebElement toggleElement = getTreeGrid().getExpandToggleElement(0, 0);
        new Actions(getDriver()).click(toggleElement).pause(300)
                .click(toggleElement).perform();

        waitUntil(d -> getTreeGrid().isRowCollapsed(0, 0));

        // expand again
        getTreeGrid().expandWithClick(0);

        // data should be visible
        verifyRow(0, "/0/0");
        verifyRow(1, "/0/0/1/0");
    }

    @Test
    public void expandAndCollapseQuickly_fastDataProvider_shouldShowChildrenWhenExpandedAgain() {
        open("nodesPerLevel=100&pageSize=25&responseDelay=0");
        setupTreeGrid();
        getTreeGrid().getCellWaitForRow(0, 0);

        // quick expand and collapse
        getTreeGrid().expandWithClick(0);
        getTreeGrid().collapseWithClick(0);
        waitUntil(d -> getTreeGrid().isRowCollapsed(0, 0));

        // expand again
        getTreeGrid().expandWithClick(0);

        // data should be visible
        verifyRow(0, "/0/0");
        verifyRow(1, "/0/0/1/0");
    }

    private void verifyRow(int rowActualIndex, String itemId) {
        Assert.assertEquals("Invalid id at index " + rowActualIndex, itemId,
                getTreeGrid().getCell(rowActualIndex, 0).getText());
    }

}
