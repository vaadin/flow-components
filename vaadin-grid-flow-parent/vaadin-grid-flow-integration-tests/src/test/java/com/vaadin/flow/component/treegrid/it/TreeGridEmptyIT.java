/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-empty")
public class TreeGridEmptyIT extends AbstractComponentIT {

    @Test
    public void empty_treegrid_initialized_correctly() {
        open();

        TreeGridElement grid = $(TreeGridElement.class).id("treegrid");

        Assert.assertTrue("Grid should be displayd", grid.isDisplayed());

        Assert.assertTrue("Grid should not have rows", grid.getRowCount() == 0);
    }
}
