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

@TestPath("vaadin-grid/treegrid-initial-expand")
public class TreeGridInitialExpandIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void initial_expand_of_items() {
        Assert.assertEquals("parent1", getTreeGrid().getCell(0, 0).getText());
        Assert.assertEquals("parent1-child1",
                getTreeGrid().getCell(1, 0).getText());
        Assert.assertEquals("parent1-child2",
                getTreeGrid().getCell(2, 0).getText());
        Assert.assertEquals("parent2", getTreeGrid().getCell(3, 0).getText());
        Assert.assertEquals("parent2-child2",
                getTreeGrid().getCell(4, 0).getText());
    }
}
