/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import java.util.Locale;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/date-picker-in-a-grid-header")
public class DatePickerInAGridHeaderPage extends Div {

    public DatePickerInAGridHeaderPage() {
        Grid<String> grid = new Grid<>();

        DatePicker header = new DatePicker();
        header.setLocale(Locale.US);
        header.setId("date-picker");

        grid.addColumn(ValueProvider.identity()).setHeader(header);
        grid.setItems(IntStream.range(0, 100).mapToObj(i -> "Item " + i));

        add(grid);
    }

}
