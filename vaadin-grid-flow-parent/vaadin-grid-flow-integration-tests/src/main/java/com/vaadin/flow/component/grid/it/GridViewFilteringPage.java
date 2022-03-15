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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/filtering")
public class GridViewFilteringPage extends LegacyTestView {

    public GridViewFilteringPage() {
        createGridWithFilters();
    }

    private void createGridWithFilters() {
        Grid<Person> grid = new Grid<>();
        ListDataProvider<Person> dataProvider = new ListDataProvider<>(
                createItems());
        grid.setDataProvider(dataProvider);

        List<ValueProvider<Person, String>> valueProviders = new ArrayList<>();
        valueProviders.add(Person::getFirstName);
        valueProviders.add(person -> String.valueOf(person.getAge()));
        valueProviders.add(person -> person.getAddress().getStreet());
        valueProviders.add(
                person -> String.valueOf(person.getAddress().getPostalCode()));

        Iterator<ValueProvider<Person, String>> iterator = valueProviders
                .iterator();

        grid.addColumn(iterator.next()).setHeader("Name");
        grid.addColumn(iterator.next()).setHeader("Age");
        grid.addColumn(iterator.next()).setHeader("Street");
        grid.addColumn(iterator.next()).setHeader("Postal Code");

        HeaderRow filterRow = grid.appendHeaderRow();

        Iterator<ValueProvider<Person, String>> iterator2 = valueProviders
                .iterator();

        grid.getColumns().forEach(column -> {
            TextField field = new TextField();
            ValueProvider<Person, String> valueProvider = iterator2.next();

            field.addValueChangeListener(event -> dataProvider
                    .addFilter(person -> StringUtils.containsIgnoreCase(
                            valueProvider.apply(person), field.getValue())));

            field.setValueChangeMode(ValueChangeMode.EAGER);

            filterRow.getCell(column).setComponent(field);
            field.setSizeFull();
            field.setPlaceholder("Filter");
        });
        grid.setId("grid-with-filters");
        addCard("Filtering", "Using text fields for filtering items", grid);
    }
}
