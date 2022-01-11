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

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/treegrid-item-details-renderer")
public class TreeGridItemDetailsRendererIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void treegridItemDetails_openCloseDetails() {
        getTreeGrid().getCell(0, 0).click();
        Assert.assertTrue(getTreeGrid().isDetailsOpen(0));
        getTreeGrid().getCell(0, 0).click();
        Assert.assertFalse(getTreeGrid().isDetailsOpen(0));
    }

    @Test
    public void treegridItemDetails_openDetailsInDifferentLevels() {
        getTreeGrid().expandWithClick(1);
        waitUntil(test -> getTreeGrid().getNumberOfExpandedRows() == 2);

        getTreeGrid().getCell(1, 0).click();
        Assert.assertTrue(getTreeGrid().isDetailsOpen(1));

        getTreeGrid().getCell(1, 0).click();
        Assert.assertFalse(getTreeGrid().isDetailsOpen(1));

        getTreeGrid().getCell(2, 0).click();
        Assert.assertTrue(getTreeGrid().isDetailsOpen(2));

        getTreeGrid().getCell(2, 0).click();
        Assert.assertFalse(getTreeGrid().isDetailsOpen(2));
        Assert.assertFalse(getTreeGrid().isDetailsOpen(0));
        Assert.assertFalse(getTreeGrid().isDetailsOpen(1));
    }

    @Test
    public void treegridItemDetails_collapseRoot_rememberOpenedDetails() {
        getTreeGrid().expandWithClick(1);
        waitUntil(test -> getTreeGrid().getNumberOfExpandedRows() == 2);

        getTreeGrid().getCell(1, 0).click();
        Assert.assertTrue(getTreeGrid().isDetailsOpen(1));

        getTreeGrid().collapseWithClick(0);
        getTreeGrid().expandWithClick(0);
        Assert.assertTrue(getTreeGrid().isDetailsOpen(1));
    }

    @Test
    public void treegridItemDetails_collapseLevel1_rememberOpenedDetails() {
        getTreeGrid().expandWithClick(1);
        waitUntil(test -> getTreeGrid().getNumberOfExpandedRows() == 2);

        getTreeGrid().getCell(2, 0).click();
        Assert.assertTrue(getTreeGrid().isDetailsOpen(2));

        getTreeGrid().collapseWithClick(1);
        getTreeGrid().expandWithClick(1);
        Assert.assertTrue(getTreeGrid().isDetailsOpen(2));
    }

}
