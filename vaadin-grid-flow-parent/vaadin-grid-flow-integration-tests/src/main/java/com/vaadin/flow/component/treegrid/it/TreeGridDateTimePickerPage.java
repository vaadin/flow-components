/*
 * Copyright 2000-2023 Vaadin Ltd.
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
        dateTimePicker.setId("id-" + item.replaceAll(" ", "-"));
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setDateFormat("dd.MM.yyyy");
        dateTimePicker.setDatePickerI18n(i18n);
        dateTimePicker.setValue(LocalDateTime.of(2000, 6, 13, 12, 0));
        return dateTimePicker;
    }
}
