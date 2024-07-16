/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/template-renderer-event-handler")
public class TemplateRendererEventHandlerPage extends Div {

    public TemplateRendererEventHandlerPage() {
        TemplateRenderer<Person> renderer = TemplateRenderer.<Person> of(
                "<div>[[item.name]] <button on-click='clicked'>Click</button></div>")
                .withProperty("name", Person::getFirstName)
                .withEventHandler("clicked", this::clicked);

        Person person = new Person("John Doe", 1981);

        Grid<Person> personGrid = new Grid<>();
        personGrid.addColumn(renderer);
        personGrid.setItems(person);

        NativeButton showHideGrid = new NativeButton("Toggle Grid Containment",
                e -> {
                    if (personGrid.getParent().isPresent()) {
                        remove(personGrid);
                    } else {
                        add(personGrid);
                    }
                });
        showHideGrid.setId("show-hide");
        add(showHideGrid);
        add(personGrid);
    }

    private void clicked(Person person) {
        Div message = new Div();
        message.addClassName("clicked-person");
        message.setText(person.getFirstName());
        add(message);
    }
}
