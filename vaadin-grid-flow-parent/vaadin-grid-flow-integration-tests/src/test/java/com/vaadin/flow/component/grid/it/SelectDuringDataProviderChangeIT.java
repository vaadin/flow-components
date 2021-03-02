/*
 * Copyright 2000-2020 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;

import java.util.function.Consumer;

@TestPath("vaadin-grid/select-during-data-provider-change")
public class SelectDuringDataProviderChangeIT extends AbstractComponentIT {

    //TODO @Test
    public void selectionDuringDataProviderChangeShouldNotCauseException()
        throws InterruptedException {
        open();
        GridElement grid = $(GridElement.class).first();
        GridTRElement row = grid.getRow(1);
        verifySelectionChangeDoesNotCauseError(row::select);
    }

    @Test
    public void deselectionDuringDataProviderChangeShouldNotCauseException()
        throws InterruptedException {
        open();
        GridElement grid = $(GridElement.class).first();
        GridTRElement row = grid.getRow(1);
        row.getCell(grid.getAllColumns().get(0)).click();
        verifySelectionChangeDoesNotCauseError(()->
            getCommandExecutor().executeScript("arguments[0].deselectItem(arguments[0].activeItem)",grid, row));
    }

    private void verifySelectionChangeDoesNotCauseError(Runnable changeSelection)
        throws InterruptedException {
        ButtonElement button = $(ButtonElement.class).first();
        // Trigger data provider change
        testBench().disableWaitForVaadin();
        button.click();
        // Click the second row
        changeSelection.run();
        getCommandExecutor().executeScript("console.log('Grid.activeItem',document.querySelector('vaadin-grid').activeItem)");
        GridElement grid = $(GridElement.class).first();
        GridTRElement row = grid.getRow(1);
        getCommandExecutor().executeScript("arguments[0].deselectItem(arguments[0].activeItem)",grid, row);
        getCommandExecutor().executeScript("console.log('Grid.activeItem',arguments[0].activeItem,arguments[1]._item,arguments[0].activeItem == arguments[1]._item)",grid, row);
        // This is not found as selection event wont be triggered
        testBench().enableWaitForVaadin();
        checkLogsForErrors();

    }
}
