/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;

@Route("vaadin-grid/tree-grid-date-time-picker")
public class TreeGridDateTimePickerPage extends Div {

    private TreeGrid<String> grid;

    public TreeGridDateTimePickerPage() {
        grid = new TreeGrid<>();
        grid.addHierarchyColumn(row -> row);
        grid.addComponentColumn(this::getDateTimePicker);

        TreeData<String> data = new TreeData<>();
        data.addRootItems("Row 1", "Row 2");
        data.addItem("Row 1", "Child 1");
        data.addItem("Row 1", "Child 2");
        data.addItem("Row 2", "Child 3");
        data.addItem("Row 2", "Child 4");
        grid.setTreeData(data);
        grid.expand("Row 1", "Row 2");

        add(grid);
    }

    private DateTimePicker getDateTimePicker(String item) {
        DateTimePicker dateTimePicker = new DateTimePicker("Date and time");
        dateTimePicker.setId("id-" + item.replace(" ", "-"));
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("dd.MM.yyyy");
        dateTimePicker.setDatePickerI18n(i18n);
        dateTimePicker.setValue(LocalDateTime.of(2000, 6, 13, 12, 0));
        return dateTimePicker;
    }
}
