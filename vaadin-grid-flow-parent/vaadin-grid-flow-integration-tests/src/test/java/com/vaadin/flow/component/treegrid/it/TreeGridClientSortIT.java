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
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/" + TreeGridBasicFeaturesPage.VIEW)
public class TreeGridClientSortIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void client_sorting_with_collapse_and_expand() {
        findElement(By.id("TreeDataProvider")).click();

        getTreeGrid().getHeaderCell(0).$("vaadin-grid-sorter").first().click();
        getTreeGrid().getHeaderCell(0).$("vaadin-grid-sorter").first().click();
        getTreeGrid().expandWithClick(0);
        getTreeGrid().expandWithClick(1);
        getTreeGrid().collapseWithClick(0);
        getTreeGrid().expandWithClick(0);
        Assert.assertEquals("0 | 2", getTreeGrid().getCell(0, 0).getText());
        Assert.assertEquals("1 | 2", getTreeGrid().getCell(1, 0).getText());
        Assert.assertEquals("2 | 2", getTreeGrid().getCell(2, 0).getText());
    }
}
