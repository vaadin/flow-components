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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.bean.Gender;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/column-resize-event")
public class ColumnResizeEventPage extends Div {

    public static final String GRID_ID = "column-resize-event-grid";
    public static final String RESIZED_COLUMN_ID = "Resized Column ID";

    public static final String RESIZED_COLUMN_ID_LABEL = "resized-col-id";
    public static final String WIDTHS_COLUMN_VALUES_LABEL = "widths-col-values";
    public static final String FLEX_GROWS_COLUMN_VALUES_LABEL = "flex-grows-col-values";

    private Label resizedColumnIdLabel = new Label();
    private Label widthsColValuesLabel = new Label();
    private Label flexGrowsColValuesLabel = new Label();

    public ColumnResizeEventPage() {
        Grid<Person> grid = new Grid<>();
        grid.setAllRowsVisible(true);
        grid.setId(GRID_ID);

        grid.setItems(new Person("Jorma", "Testaaja", "jorma@testaaja.com",
                2018, Gender.MALE, null));

        Column<Person> firstNameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("A").setResizable(true);
        firstNameColumn.setId("A");

        Column<Person> lastNameColumn = grid.addColumn(Person::getLastName)
                .setHeader("B").setResizable(true);
        lastNameColumn.setId(RESIZED_COLUMN_ID);

        Column<Person> idColumn = grid.addColumn(Person::getId).setHeader("C")
                .setResizable(true);
        idColumn.setId("C");

        grid.addColumnResizeListener(e -> {
            resizedColumnIdLabel.setText(e.getResizedColumn().getId().get());
            flexGrowsColValuesLabel.setText(firstNameColumn.getFlexGrow() + "|"
                    + lastNameColumn.getFlexGrow() + "|"
                    + idColumn.getFlexGrow());
            widthsColValuesLabel.setText(firstNameColumn.getWidth() + "|"
                    + lastNameColumn.getWidth() + "|" + idColumn.getWidth());
        });

        resizedColumnIdLabel.setId(RESIZED_COLUMN_ID_LABEL);
        flexGrowsColValuesLabel.setId(FLEX_GROWS_COLUMN_VALUES_LABEL);
        widthsColValuesLabel.setId(WIDTHS_COLUMN_VALUES_LABEL);

        add(grid, resizedColumnIdLabel, widthsColValuesLabel,
                flexGrowsColValuesLabel);
    }

}
