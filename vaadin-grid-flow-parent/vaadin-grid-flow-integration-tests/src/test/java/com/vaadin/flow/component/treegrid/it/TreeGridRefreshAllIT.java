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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-refresh-all")
public class TreeGridRefreshAllIT extends AbstractTreeGridIT {

    @Before
    public void init() {
        open();
        setupTreeGrid();
    }

    @Test // https://github.com/vaadin/vaadin-grid-flow/issues/589
    public void expandMultipleLevels_refreshAllTwice_cellsRendered() {
        getTreeGrid().expandWithClick(0);
        getTreeGrid().expandWithClick(1);
        getTreeGrid().expandWithClick(2);
        getTreeGrid().expandWithClick(3);

        WebElement button = findElement(By.id("refresh-all"));
        button.click();
        button.click();

        assertCellTexts(0, 0,
                new String[] { "0 | 0", "1 | 0", "2 | 0", "3 | 0", "4 | 0" });
    }

}
