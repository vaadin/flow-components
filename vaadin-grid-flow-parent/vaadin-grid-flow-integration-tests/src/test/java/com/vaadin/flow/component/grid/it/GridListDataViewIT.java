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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ADD_EXTRA_PERSONS_FOR_SORTING;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ADD_ITEM;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ADD_SERVER_SIDE_SORTING;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.DELETE_ITEM;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.FIRST_GRID_ID;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.FIRST_NAME_FILTER;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ITEM_COUNT;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ITEM_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.REMOVE_SERVER_SIDE_SORTING;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.ROW_SELECT;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SECOND_GRID_ID;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SHOW_ITEM_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SHOW_NEXT_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.SHOW_PREVIOUS_DATA;
import static com.vaadin.flow.component.grid.it.GridListDataViewPage.UPDATE_ITEM;

@TestPath("vaadin-grid/grid-list-data-view-page")
public class GridListDataViewIT extends AbstractComponentIT {

    private GridElement firstGrid;
    private GridElement secondGrid;

    @Before
    public void init() {
        open();

        firstGrid = $(GridElement.class).id(FIRST_GRID_ID);
        secondGrid = $(GridElement.class).id(SECOND_GRID_ID);
    }

    @Test
    public void getItemCount_showsInitialItemsCount() {
        Assert.assertEquals("Item count not expected", "250",
                $("span").id(ITEM_COUNT).getText());
    }

    @Test
    public void navigateBetweenItems_showsCorrectItems() {
        Assert.assertEquals("Initial selection should be 0", "0",
                $(IntegerFieldElement.class).id(ROW_SELECT).getValue());

        Assert.assertFalse("Item row 0 should not have previous data.",
                $(ButtonElement.class).id(SHOW_PREVIOUS_DATA).isEnabled());
        Assert.assertTrue("Item row 0 has next data.",
                $(ButtonElement.class).id(SHOW_NEXT_DATA).isEnabled());

        $(ButtonElement.class).id(SHOW_ITEM_DATA).click();

        Assert.assertEquals("Item: Person 1",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_NEXT_DATA).click();
        Assert.assertEquals("Item: Person 2",
                $("span").id(ITEM_DATA).getText());

        $(IntegerFieldElement.class).id(ROW_SELECT).setValue("5");

        $(ButtonElement.class).id(SHOW_ITEM_DATA).click();
        Assert.assertEquals("Wrong row item", "Item: Person 6",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_NEXT_DATA).click();
        Assert.assertEquals("Wrong next item.", "Item: Person 7",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_PREVIOUS_DATA).click();
        Assert.assertEquals("Wrong previous item.", "Item: Person 5",
                $("span").id(ITEM_DATA).getText());
    }

    @Test
    public void sorting_setClientThenServerSorting_bothSortingsApplied() {
        // Sort highest first. NOTE! this means that we start with 99
        applyClientSortByFirstName();

        // Set the second row (from top) as the current item
        $(IntegerFieldElement.class).id(ROW_SELECT).setValue("1");

        $(ButtonElement.class).id(SHOW_ITEM_DATA).click();
        Assert.assertEquals("Wrong row item for sorted data", "Item: Person 98",
                $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_NEXT_DATA).click();
        Assert.assertEquals("Wrong next item for sorted data.",
                "Item: Person 97", $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(SHOW_PREVIOUS_DATA).click();
        Assert.assertEquals("Wrong previous item for sorted data.",
                "Item: Person 99", $("span").id(ITEM_DATA).getText());

        $(ButtonElement.class).id(ADD_EXTRA_PERSONS_FOR_SORTING).click();

        $(ButtonElement.class).id(ADD_SERVER_SIDE_SORTING).click();

        Assert.assertEquals("Unexpected first name on row 0", "Person 99",
                getFirstNameByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected first name on row 1", "Person 99",
                getFirstNameByRow(firstGrid, 1));
        Assert.assertEquals("Unexpected first name on row 2", "Person 99",
                getFirstNameByRow(firstGrid, 2));

        Assert.assertEquals("Unexpected age on row 0", "42",
                getAgeByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected age on row 1", "24",
                getAgeByRow(firstGrid, 1));
        Assert.assertEquals("Unexpected age on row 2", "18",
                getAgeByRow(firstGrid, 2));

        $(ButtonElement.class).id(REMOVE_SERVER_SIDE_SORTING).click();

        Assert.assertEquals("Unexpected first name on row 0", "Person 99",
                getFirstNameByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected first name on row 1", "Person 99",
                getFirstNameByRow(firstGrid, 1));
        Assert.assertEquals("Unexpected first name on row 2", "Person 99",
                getFirstNameByRow(firstGrid, 2));

        Assert.assertEquals("Unexpected age on row 0", "24",
                getAgeByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected age on row 1", "18",
                getAgeByRow(firstGrid, 1));
        Assert.assertEquals("Unexpected age on row 2", "42",
                getAgeByRow(firstGrid, 2));
    }

    @Test
    public void sorting_setServerThenClientSorting_bothSortingsApplied() {
        $(ButtonElement.class).id(ADD_EXTRA_PERSONS_FOR_SORTING).click();
        $(ButtonElement.class).id(ADD_SERVER_SIDE_SORTING).click();

        // Person 89
        Assert.assertEquals("Unexpected age on row 0", "104",
                getAgeByRow(firstGrid, 0));
        // Person 179
        Assert.assertEquals("Unexpected age on row 1", "104",
                getAgeByRow(firstGrid, 1));
        // Person 88
        Assert.assertEquals("Unexpected age on row 2", "103",
                getAgeByRow(firstGrid, 2));
        // Person 178
        Assert.assertEquals("Unexpected age on row 3", "103",
                getAgeByRow(firstGrid, 3));

        // Sort highest first. NOTE! this means that we start with 99
        // Client sort should have higher priority
        applyClientSortByFirstName();

        Assert.assertEquals("Unexpected first name on row 0", "Person 99",
                getFirstNameByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected first name on row 1", "Person 99",
                getFirstNameByRow(firstGrid, 1));
        Assert.assertEquals("Unexpected first name on row 2", "Person 99",
                getFirstNameByRow(firstGrid, 2));

        Assert.assertEquals("Unexpected age on row 0", "42",
                getAgeByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected age on row 1", "24",
                getAgeByRow(firstGrid, 1));
        Assert.assertEquals("Unexpected age on row 2", "18",
                getAgeByRow(firstGrid, 2));
    }

    @Test
    public void sorting_setSortingToFirstGrid_secondGridNotImpacted() {
        $(ButtonElement.class).id(ADD_SERVER_SIDE_SORTING).click();

        Assert.assertEquals("Unexpected first name on row 0 in first grid",
                "Person 89", getFirstNameByRow(firstGrid, 0));
        Assert.assertEquals("Unexpected age on row 0 in first grid", "104",
                getAgeByRow(firstGrid, 0));

        Assert.assertEquals("Unexpected first name on row 0 in second grid",
                "Person 1", getFirstNameByRow(secondGrid, 0));
        Assert.assertEquals("Unexpected age on row 0 in second grid", "16",
                getAgeByRow(secondGrid, 0));
    }

    @Test
    public void addRemoveAndUpdateItem_addsThenUpdatesThenRemovesItem() {
        $(ButtonElement.class).id(ADD_ITEM).click();
        Assert.assertEquals("Item count not expected", "251",
                $("span").id(ITEM_COUNT).getText());
        Assert.assertEquals("Wrong first name for added person", "John",
                firstGrid.getCell(250, 0).getText());
        Assert.assertEquals("Wrong last name for added person", "Doe",
                firstGrid.getCell(250, 1).getText());
        Assert.assertEquals("Wrong age for added person", "33",
                firstGrid.getCell(250, 2).getText());

        $(IntegerFieldElement.class).id(ROW_SELECT).setValue("250");
        $(TextFieldElement.class).id(UPDATE_ITEM).setValue("Bob");
        Assert.assertEquals("Wrong first name for updated person", "Bob",
                firstGrid.getCell(250, 0).getText());
        Assert.assertEquals("Wrong last name for updated person", "Doe",
                firstGrid.getCell(250, 1).getText());
        Assert.assertEquals("Wrong age for updated person", "33",
                firstGrid.getCell(250, 2).getText());

        $(IntegerFieldElement.class).id(ROW_SELECT).setValue("250");
        $(ButtonElement.class).id(DELETE_ITEM).click();
        Assert.assertEquals("Item count not expected", "250",
                $("span").id(ITEM_COUNT).getText());
    }

    @Test
    public void setFilter_serverSideFilterAppliedToFirstGridAndNotForSecond() {
        $(TextFieldElement.class).id(FIRST_NAME_FILTER).setValue("9");

        // There are 43 firstnames with a 9 in the set from 1-250
        Assert.assertEquals("Filtered size not as expected", "43",
                $("span").id(ITEM_COUNT).getText());

        // Verify the first grid contains filtered set of items
        Assert.assertEquals(43, firstGrid.getRowCount());

        // Verify the second grid contains non-filtered set of items
        Assert.assertEquals(250, secondGrid.getRowCount());

        // Reset the filter
        $(TextFieldElement.class).id(FIRST_NAME_FILTER).setValue("");

        // Verify that both grids have 250 items
        Assert.assertEquals("Filtered size not as expected", "250",
                $("span").id(ITEM_COUNT).getText());

        Assert.assertEquals(250, firstGrid.getRowCount());

        Assert.assertEquals(250, secondGrid.getRowCount());
    }

    private void applyClientSortByFirstName() {
        firstGrid.getHeaderCell(0).$("vaadin-grid-sorter").first().click();
        firstGrid.getHeaderCell(0).$("vaadin-grid-sorter").first().click();
    }

    private String getFirstNameByRow(GridElement grid, int row) {
        return grid.getRow(row).getCell(grid.getColumn("First Name")).getText();
    }

    private String getAgeByRow(GridElement grid, int row) {
        return grid.getRow(row).getCell(grid.getColumn("Age")).getText();
    }

}
