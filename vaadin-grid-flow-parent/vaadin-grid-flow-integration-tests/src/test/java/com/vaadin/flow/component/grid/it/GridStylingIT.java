/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-grid/grid-styling")
public class GridStylingIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-grid"));
        grid = $(GridElement.class).first();
    }

    @Test
    public void noClassesOnCellsInitially() {
        assertCellClassNames( //
                "", "", //
                "", "", //
                "", "");
    }

    @Test
    public void grid_setClassNameGenerator_classesGenerated() {
        click("grid-generator");
        assertCellClassNames( //
                "grid0", "grid0", //
                "grid1", "grid1", //
                "grid2", "grid2"); //
    }

    @Test
    public void column_setClassNameGenerator_classesGeneratedOnlyForThatColumn() {
        click("column-generator");
        assertCellClassNames( //
                "col0", "", //
                "col1", "", //
                "col2", "");
    }

    @Test
    public void setClassNameGeneratorsForBothColumns_classesGenerated() {
        click("column-generator");
        click("second-column-generator");
        assertCellClassNames( //
                "col0", "baz", //
                "col1", "baz", //
                "col2", "baz");
    }

    @Test
    public void grid_changeClassNameGeneratorToReturnNull_classesRemoved() {
        click("grid-generator");
        click("reset-grid-generator");
        assertCellClassNames( //
                "", "", //
                "", "", //
                "", "");
    }

    @Test
    public void column_changeClassNameGeneratorToReturnNull_classesRemoved() {
        click("column-generator");
        click("reset-column-generator");
        assertCellClassNames( //
                "", "", //
                "", "", //
                "", "");
    }

    @Test
    public void grid_generateMultipleClasses_allIncluded() {
        click("grid-multiple-classes");
        assertCellClassNames( //
                "grid foo", "grid foo", //
                "grid foo", "grid foo", //
                "grid foo", "grid foo");
    }

    @Test
    public void column_generateMultipleClasses_allIncluded() {
        click("column-multiple-classes");
        assertCellClassNames( //
                "col bar", "", //
                "col bar", "", //
                "col bar", "");
    }

    @Test
    public void setGridAndColumnClassNameGenerators_bothEffective_gridClassesFirst() {
        click("grid-generator");
        click("column-generator");
        assertCellClassNames( //
                "grid0 col0", "grid0", //
                "grid1 col1", "grid1", //
                "grid2 col2", "grid2");
    }

    @Test
    public void setColumnAndGridClassNameGenerators_bothEffective_gridClassesFirst() {
        click("column-generator");
        click("grid-generator");
        assertCellClassNames( //
                "grid0 col0", "grid0", //
                "grid1 col1", "grid1", //
                "grid2 col2", "grid2");
    }

    @Test
    public void generateMultipleClassesViaColumnAndGrid_allIncluded_gridClassesFirst() {
        click("column-multiple-classes");
        click("grid-multiple-classes");
        assertCellClassNames( //
                "grid foo col bar", "grid foo", //
                "grid foo col bar", "grid foo", //
                "grid foo col bar", "grid foo");
    }

    @Test
    public void setGridAndColumnClassNameGenerators_detach_attach_classesEffective() {
        click("grid-generator");
        click("column-generator");

        click("toggle-attached");
        click("toggle-attached");
        grid = $(GridElement.class).first();

        assertCellClassNames( //
                "grid0 col0", "grid0", //
                "grid1 col1", "grid1", //
                "grid2 col2", "grid2");
    }

    @Test
    public void setItemDetailsRenderer_gridClassNameGeneratorAppliedToDetailsRow() {
        click("details-renderer");
        click("grid-generator");
        click("column-generator");

        Assert.assertEquals("grid0",
                grid.getRow(0).getDetailsRow().getAttribute("class"));
        Assert.assertEquals("grid5",
                grid.getRow(5).getDetailsRow().getAttribute("class"));

        checkLogsForErrors();
    }

    @Test
    public void noCustomPartsOnCellsInitially() {
        assertCellPartNames( //
                "", "", //
                "", "", //
                "", "");
    }

    @Test
    public void grid_setPartNameGenerator_partsGenerated() {
        click("grid-part-generator");
        assertCellPartNames( //
                "grid0", "grid0", //
                "grid1", "grid1", //
                "grid2", "grid2"); //
    }

    @Test
    public void column_setPartNameGenerator_partsGeneratedOnlyForThatColumn() {
        click("column-part-generator");
        assertCellPartNames( //
                "col0", "", //
                "col1", "", //
                "col2", "");
    }

    @Test
    public void setPartNameGeneratorForBothColumns_partsGenerated() {
        click("column-part-generator");
        click("second-column-part-generator");
        assertCellPartNames( //
                "col0", "baz", //
                "col1", "baz", //
                "col2", "baz");
    }

    @Test
    public void grid_changePartNameGeneratorToReturnNull_partsRemoved() {
        click("grid-part-generator");
        click("reset-grid-part-generator");
        assertCellPartNames( //
                "", "", //
                "", "", //
                "", "");
    }

    @Test
    public void column_changePartNameGeneratorToReturnNull_partsRemoved() {
        click("column-part-generator");
        click("reset-column-part-generator");
        assertCellPartNames( //
                "", "", //
                "", "", //
                "", "");
    }

    @Test
    public void grid_generateMultipleParts_allIncluded() {
        click("grid-multiple-parts");
        assertCellPartNames( //
                "grid foo", "grid foo", //
                "grid foo", "grid foo", //
                "grid foo", "grid foo");
    }

    @Test
    public void column_generateMultipleParts_allIncluded() {
        click("column-multiple-parts");
        assertCellPartNames( //
                "col bar", "", //
                "col bar", "", //
                "col bar", "");
    }

    @Test
    public void setGridAndColumnPartNameGenerators_bothEffective_gridPartsFirst() {
        click("grid-part-generator");
        click("column-part-generator");
        assertCellPartNames( //
                "grid0 col0", "grid0", //
                "grid1 col1", "grid1", //
                "grid2 col2", "grid2");
    }

    @Test
    public void setColumnAndGridPartNameGenerators_bothEffective_gridPartsFirst() {
        click("column-part-generator");
        click("grid-part-generator");
        assertCellPartNames( //
                "grid0 col0", "grid0", //
                "grid1 col1", "grid1", //
                "grid2 col2", "grid2");
    }

    @Test
    public void generateMultiplePartsViaColumnAndGrid_allIncluded_gridPartsFirst() {
        click("column-multiple-parts");
        click("grid-multiple-parts");
        assertCellPartNames( //
                "grid foo col bar", "grid foo", //
                "grid foo col bar", "grid foo", //
                "grid foo col bar", "grid foo");
    }

    @Test
    public void setGridAndColumnPartNameGenerators_detach_attach_partsEffective() {
        click("grid-part-generator");
        click("column-part-generator");

        click("toggle-attached");
        click("toggle-attached");
        grid = $(GridElement.class).first();

        assertCellPartNames( //
                "grid0 col0", "grid0", //
                "grid1 col1", "grid1", //
                "grid2 col2", "grid2");
    }

    /**
     * Compares each class to the cell at the corresponding index
     */
    private void assertCellClassNames(String... expectedClassNames) {
        for (int i = 0; i < expectedClassNames.length; i++) {
            assertCellClassNames(grid, i / 2, i % 2, expectedClassNames[i]);
        }
    }

    static void assertCellClassNames(GridElement grid, int rowIndex,
            int colIndex, String expectedClassNames) {
        String classNames = grid.getCell(rowIndex, colIndex)
                .getAttribute("class");
        Assert.assertEquals(String.format(
                "Unexpected class names in cell at row %s, col %s.", rowIndex,
                colIndex), expectedClassNames, classNames);
    }

    /**
     * Compares each part to the cell at the corresponding index
     */
    private void assertCellPartNames(String... expectedPartNames) {
        for (int i = 0; i < expectedPartNames.length; i++) {
            assertCellPartNames(grid, i / 2, i % 2, expectedPartNames[i]);
        }
    }

    static void assertCellPartNames(GridElement grid, int rowIndex,
            int colIndex, String expectedPartNames) {
        String partNames = grid.getCell(rowIndex, colIndex)
                .getAttribute("part");
        String customParts = Stream.of(partNames.split(" "))
                .filter(part -> !part.contains("cell"))
                .collect(Collectors.joining(" "));

        Assert.assertEquals(String.format(
                "Unexpected part names in cell at row %s, col %s.", rowIndex,
                colIndex), expectedPartNames, customParts);
    }

    private void click(String id) {
        $(TestBenchElement.class).id(id).click();
    }

}
