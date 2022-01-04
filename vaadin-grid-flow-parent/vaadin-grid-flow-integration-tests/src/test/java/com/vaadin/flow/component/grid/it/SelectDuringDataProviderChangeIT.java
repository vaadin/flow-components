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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Test;

@TestPath("vaadin-grid/select-during-data-provider-change")
public class SelectDuringDataProviderChangeIT extends AbstractComponentIT {

    @Test
    public void selectionDuringDataProviderChangeShouldNotCauseException() {
        open();
        GridElement grid = $(GridElement.class).first();
        GridTRElement row = grid.getRow(1);
        verifySelectionChangeDoesNotCauseError(row::select);
    }

    @Test
    public void deselectionDuringDataProviderChangeShouldNotCauseException() {
        open();
        GridElement grid = $(GridElement.class).first();
        GridTRElement row = grid.getRow(1);
        row.select();
        verifySelectionChangeDoesNotCauseError(row::deselect);
    }

    private void verifySelectionChangeDoesNotCauseError(
            Runnable changeSelection) {
        ButtonElement button = $(ButtonElement.class).first();
        testBench().disableWaitForVaadin();
        // Trigger data provider change
        button.click();
        // Change row selection
        changeSelection.run();
        testBench().enableWaitForVaadin();
        checkLogsForErrors();
    }
}
