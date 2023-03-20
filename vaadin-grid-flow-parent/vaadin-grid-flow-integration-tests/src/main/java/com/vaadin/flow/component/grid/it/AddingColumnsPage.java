/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;

/**
 * Test page for dynamically adding new columns with different renderers after
 * the Grid has already been attached and rendered.
 */
@Route("vaadin-grid/adding-columns")
public class AddingColumnsPage extends Div {

    private Grid<Person> grid = new Grid<>();

    public AddingColumnsPage() {
        grid.setItems(new Person("foo", 20), new Person("bar", 30));
        add(grid);

        addButton("add-value-provider-column",
                () -> grid.addColumn(Person::getFirstName));

        addButton("add-template-column",
                TemplateRenderer.<Person> of("<div>[[item.age]]</div>")
                        .withProperty("age", Person::getAge));

        addButton("add-component-column", new ComponentRenderer<>(
                person -> new Label(person.getFirstName())));

        addButton("add-number-column", new NumberRenderer<>(Person::getAge,
                NumberFormat.getIntegerInstance()));

        addButton("add-local-date-column", new LocalDateRenderer<>(
                person -> LocalDate.of(1990, 1, person.getAge())));

        addButton("add-local-date-time-column", new LocalDateTimeRenderer<>(
                person -> LocalDateTime.of(1980, 1, 1, 1, person.getAge())));

        addButton("add-button-column",
                new NativeButtonRenderer<>("click", person -> {
                    // NO-OP
                }));
    }

    private void addButton(String id, Renderer<Person> renderer) {
        addButton(id, () -> grid.addColumn(renderer));
    }

    private void addButton(String id, Command action) {
        NativeButton button = new NativeButton(id, e -> action.execute());
        button.setId(id);
        add(button);
    }

}
