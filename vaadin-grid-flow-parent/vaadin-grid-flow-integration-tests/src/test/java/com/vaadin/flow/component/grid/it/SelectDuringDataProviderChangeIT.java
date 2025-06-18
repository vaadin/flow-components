/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
