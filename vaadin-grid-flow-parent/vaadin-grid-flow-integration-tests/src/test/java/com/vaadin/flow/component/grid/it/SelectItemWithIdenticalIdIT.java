/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-grid/select-item-with-identical-id")
public class SelectItemWithIdenticalIdIT extends AbstractComponentIT {

    private CheckboxElement useMultiSelectCheckbox;

    private ButtonElement updateSelectionButton;

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).waitForFirst();
        useMultiSelectCheckbox = $(CheckboxElement.class).first();
        updateSelectionButton = $(ButtonElement.class).first();
    }

    @Test
    public void singleSelectGrid_selectItemWithSameIdBeforeRender_itemInDataProviderIsRendered() {
        Assert.assertEquals("1", grid.getCell(0, 0).getText());
    }

    @Test
    public void singleSelectGrid_selectItemWithSameIdAfterRender_itemInDataProviderIsRendered() {
        updateSelectionButton.click();

        Assert.assertEquals("2", grid.getCell(1, 0).getText());
    }

    @Test
    public void multiSelectGrid_selectItemWithSameIdBeforeRender_itemInDataProviderIsRendered() {
        useMultiSelectCheckbox.setChecked(true);

        Assert.assertEquals("1", grid.getCell(0, 1).getText());
    }

    @Test
    public void multiSelectGrid_selectItemWithSameIdAfterRender_itemInDataProviderIsRendered() {
        useMultiSelectCheckbox.setChecked(true);
        updateSelectionButton.click();

        Assert.assertEquals("1", grid.getCell(0, 1).getText());
    }
}
