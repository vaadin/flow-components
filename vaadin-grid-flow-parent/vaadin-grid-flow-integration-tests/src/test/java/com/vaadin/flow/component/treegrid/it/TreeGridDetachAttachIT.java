/*
 * Copyright 2000-2019 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/treegrid-detach-attach")
public class TreeGridDetachAttachIT extends AbstractComponentIT {

    private TreeGridElement grid;
    private TestBenchElement toggleAttachedButton;

    @Before
    public void before() {
        open();
        grid = $(TreeGridElement.class).first();
        toggleAttachedButton = $("button").id("toggle-attached");
    }

    @Test
    public void scrollDown_detach_attach_firstItemsRendered() {
        grid.scrollToRow(150);

        toggleAttachedButton.click();
        toggleAttachedButton.click();

        grid = $(TreeGridElement.class).first();
        Assert.assertEquals("0 | 0", grid.getCell(0, 0).getText());
    }

}
