/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Test page for dynamically adding new columns with different renderers when
 * the Grid is added.
 */
@Route("vaadin-grid/component-columns")
@Theme(Lumo.class)
public class ComponentColumnsPage extends Div {

    public ComponentColumnsPage() {
        addButton("btn-add-comp-then-grid", () -> {
            Grid<Person> compThenGrid = new Grid<>();
            compThenGrid.setId("comp-then-grid");
            compThenGrid.setItems(new Person("foo", 20), new Person("bar", 30));

            compThenGrid.addColumn(
                    new NativeButtonRenderer<>("click", this::clicked));
            compThenGrid.addColumn(new ComponentRenderer<>(
                    person -> new Button(person.getFirstName())));
            compThenGrid.addColumn(new ComponentRenderer<>(
                    person -> new Button("age " + person.getAge())));

            add(compThenGrid);
        });
        addButton("btn-add-grid-then-comp", () -> {
            Grid<Person> gridThenComp = new Grid<>();
            gridThenComp.setId("grid-then-comp");
            gridThenComp.setItems(new Person("foo", 20), new Person("bar", 30));

            add(gridThenComp);

            gridThenComp.addColumn(
                    new NativeButtonRenderer<>("click", this::clicked));
            gridThenComp.addColumn(new ComponentRenderer<>(
                    person -> new Button(person.getFirstName())));
            gridThenComp.addColumn(new ComponentRenderer<>(
                    person -> new Button("age " + person.getAge())));
        });
    }

    private void addButton(String id, Command action) {
        NativeButton button = new NativeButton(id, e -> action.execute());
        button.setId(id);
        add(button);
    }

    private void clicked(Person person) {
        Div message = new Div();
        message.addClassName("clicked-person");
        message.setText(person.getFirstName());
        add(message);
    }
}
