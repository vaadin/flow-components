
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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Route("vaadin-grid/remove-sortable-column")
public class RemoveSortableColumnPage extends VerticalLayout {

    public static final String ID_SORT_BUTTON = "sort-button";

    private MyGrid grid;
    private Select<String>[] selects;

    private final List<String> headers = Arrays.asList("firstName", "lastName",
            "age", "gender", "nickname");
    private final Map<String, ValueProvider<Person, ?>> headerToValueProvider = new HashMap<String, ValueProvider<Person, ?>>() {
        {
            put("firstName", person -> person.firstName);
            put("lastName", person -> person.lastName);
            put("age", person -> person.age);
            put("gender", person -> person.gender);
            put("nickname", person -> person.nickname);
        }
    };
    private final Person[] people = new Person[] {
            new Person("Ben", "Hanks", 25, "Male", "Bensy"),
            new Person("Zackhary", "Smith", 26, "Male", "Zacsy"),
            new Person("Tom", "Jones", 27, "Male", "Tomsy"),
            new Person("Jerry", "Stallone", 28, "Male", "Jersey"),
            new Person("Bob", "Rourke", 29, "Male", "Bobsy") };

    class Person {
        String firstName;
        String lastName;
        Integer age;
        String gender;
        String nickname;

        Person(String firstName, String lastName, Integer age, String gender,
                String nickname) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.gender = gender;
            this.nickname = nickname;
        }
    }

    class MyGrid extends Grid<Person> {

        private Function<Integer, Component> headerGenerator;
        private HeaderRow headerRow;

        public void setHeaderGenerator(
                Function<Integer, Component> headerGenerator) {
            headerRow = addFirstHeaderRow();
            this.headerGenerator = headerGenerator;
            redraw();
        }

        void setData() {
            setItems(people);
        }

        public void redraw() {
            setData();
            addColumnsToContainer();
            getDataCommunicator().reset();
        }

        void addColumnsToContainer() {
            removeAllColumns();
            for (int i = 0; i < 5; ++i) {
                String value = selects[i].getValue();
                addColumn(headerToValueProvider.get(value)).setSortable(true);
                headerRow.getCells().get(i)
                        .setComponent(headerGenerator.apply(i));
            }
        }
    }

    public Function<Integer, Component> getHeaderGenerator() {
        return columnIndex -> selects[columnIndex];
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        attachEvent.getUI().getSession().setErrorHandler(handler -> {
            Span text = new Span("Error");
            text.setId("error-handler-message");
            add(text);
        });
    }

    public RemoveSortableColumnPage() {
        selects = new Select[5];
        for (int i = 0; i < 5; ++i) {
            Select<String> select = new Select<>();
            select.setId("select" + i);
            select.setItems(headers);
            select.setValue(headers.get(i));
            select.getElement().addEventListener("click", e -> {
            }).addEventData("event.stopPropagation()");
            select.addValueChangeListener(e -> {
                grid.redraw();
            });
            selects[i] = select;

        }
        grid = new MyGrid();
        grid.setHeaderGenerator(getHeaderGenerator());

        add(grid);

        Button sort = new Button("Sort");
        sort.setId(ID_SORT_BUTTON);
        sort.addClickListener(event -> {
            GridSortOrderBuilder<Person> sortOrderBuilder = new GridSortOrderBuilder<>();
            sortOrderBuilder.thenAsc(grid.getColumns().get(0));
            grid.sort(sortOrderBuilder.build());
        });
        add(sort);
    }

}
