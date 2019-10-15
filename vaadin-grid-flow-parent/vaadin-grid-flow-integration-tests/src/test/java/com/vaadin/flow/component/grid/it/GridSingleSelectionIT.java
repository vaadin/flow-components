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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-single-selection")
public class GridSingleSelectionIT extends AbstractComponentIT {

    @Test
    public void checkDeselectionAllowedByDefault() {
        open();

        // Ensure that de-selection is allowed by default
        GridElement grid = $(GridElement.class)
                .id(GridSingleSelectionPage.DESELECT_ALLOWED_GRID_ID);

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue("Row 1 was still selected after de-selecting it.",
                !grid.getRow(1).isSelected());

        // Disable de-selection on the fly and test again
        $("button").id(GridSingleSelectionPage.DESELECT_ALLOWED_TOGGLE_ID)
                .click();

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue(
                "Row 1 was deselected even though deselection is not allowed.",
                grid.getRow(1).isSelected());
    }

    @Test
    public void checkDeselectionDisallowedInitially() {
        open();

        // Ensure that de-selection is not possible when it has been disallowed
        // initially
        GridElement grid = $(GridElement.class)
                .id(GridSingleSelectionPage.DESELECT_DISALLOWED_GRID_ID);

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue(
                "Row 1 was deselected even though deselection is not allowed.",
                grid.getRow(1).isSelected());

        // Enable de-selection on the fly and test again
        $("button").id(GridSingleSelectionPage.DESELECT_DISALLOWED_TOGGLE_ID)
                .click();

        grid.getRow(1).select();
        Assert.assertTrue("Row 1 was not selected after selecting it.",
                grid.getRow(1).isSelected());

        grid.getRow(1).deselect();
        Assert.assertTrue("Row 1 was still selected after de-selecting it.",
                !grid.getRow(1).isSelected());
    }
}