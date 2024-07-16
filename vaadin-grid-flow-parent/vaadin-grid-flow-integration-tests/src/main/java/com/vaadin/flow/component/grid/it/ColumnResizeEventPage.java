/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.Map;

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
