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
