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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/header-page")
public class HeaderTextPage extends Div {

    public static final String CHANGE_ADDRESS_HEADER_ID = "changeAddressHeader";
    public static final String CHANGE_AGE_HEADER_ID = "changeAgeHeader";
    public static final String CHANGE_HEADER_IN_JOINED_CELLS_ID = "changeHeaderInJoinedCells";
    public static final String CHANGE_HEADER_IN_MIDDLE_ID = "changeHeaderInMiddle";
    public static final String CHANGE_HEADER_IN_LAST_ROW_ID = "changeHeaderInLastRow";
    public static final String SINGLE_HEADER_GRID_ID = "singleHeaderGrid";
    public static final String MULTI_HEADER_ROWS_GRID_ID = "multiHeaderRowsGrid";

    public HeaderTextPage() {
        Grid<Person> singleHeaderGrid = new Grid<>(Person.class);
        singleHeaderGrid.setId(SINGLE_HEADER_GRID_ID);
        singleHeaderGrid.getThemeNames().add("column-borders");
        singleHeaderGrid.setItems(Person.createTestPerson1(),
                Person.createTestPerson2());

        NativeButton changeAddressHeader = new NativeButton(
                CHANGE_ADDRESS_HEADER_ID);
        changeAddressHeader.setId(CHANGE_ADDRESS_HEADER_ID);
        changeAddressHeader.addClickListener(event -> singleHeaderGrid
                .getColumns().get(0).setHeader("Addr."));

        NativeButton changeAgeHeader = new NativeButton(CHANGE_AGE_HEADER_ID);
        changeAgeHeader.setId(CHANGE_AGE_HEADER_ID);
        changeAgeHeader.addClickListener(event -> singleHeaderGrid.getColumns()
                .get(1).setHeader("Birth Year"));
        add(singleHeaderGrid, changeAddressHeader, changeAgeHeader);

        Grid<Person> multiHeaderRowsGrid = new Grid<>(Person.class);
        multiHeaderRowsGrid.setId(MULTI_HEADER_ROWS_GRID_ID);
        multiHeaderRowsGrid.setItems(Person.createTestPerson1(),
                Person.createTestPerson2());
        HeaderRow topHeaderRow = multiHeaderRowsGrid.prependHeaderRow();
        topHeaderRow.getCells().get(0).setText("abc");
        topHeaderRow.join(topHeaderRow.getCells().get(1),
                topHeaderRow.getCells().get(2)).setText("xyz");
        HeaderRow bottomHeaderRow = multiHeaderRowsGrid.appendHeaderRow();
        bottomHeaderRow.getCells().get(3).setText("abc");

        NativeButton changeHeaderInJoinedCells = new NativeButton(
                CHANGE_HEADER_IN_JOINED_CELLS_ID);
        changeHeaderInJoinedCells.setId(CHANGE_HEADER_IN_JOINED_CELLS_ID);
        changeHeaderInJoinedCells
                .addClickListener(event -> multiHeaderRowsGrid.getHeaderRows()
                        .get(0).getCells().get(1).setText("New Header"));

        NativeButton changeHeaderInMiddle = new NativeButton(
                CHANGE_HEADER_IN_MIDDLE_ID);
        changeHeaderInMiddle.setId(CHANGE_HEADER_IN_MIDDLE_ID);
        changeHeaderInMiddle.addClickListener(event -> multiHeaderRowsGrid
                .getColumns().get(1).setHeader("Afterlife"));

        NativeButton changeHeaderInLastRow = new NativeButton(
                CHANGE_HEADER_IN_LAST_ROW_ID);
        changeHeaderInLastRow.setId(CHANGE_HEADER_IN_LAST_ROW_ID);
        changeHeaderInLastRow.addClickListener(event -> bottomHeaderRow
                .getCells().get(3).setText("Something"));

        add(multiHeaderRowsGrid, changeHeaderInJoinedCells,
                changeHeaderInMiddle, changeHeaderInLastRow);
    }
}
