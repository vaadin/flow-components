/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/click-listeners")
public class GridViewClickListenersPage extends LegacyTestView {

    public GridViewClickListenersPage() {
        createClickListener();
        createDoubleClickListener();
    }

    private void createClickListener() {
        Div message = new Div();
        message.setId("clicked-item");

        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        grid.addColumn(new ComponentRenderer<>(person -> {
            Span span = new Span("Action");
            span.getElement().executeJs(
                    "$0.addEventListener('click', e => e.preventDefault())");
            return span;
        })).setHeader("Action");

        // Disable selection: will receive only click events instead
        grid.setSelectionMode(SelectionMode.NONE);

        grid.addItemClickListener(event -> message
                .setText("Clicked Item: " + event.getItem().getFirstName()));

        grid.setId("item-click-listener");

        message.addClickListener(event -> message.setText(""));
        addCard("Click Listeners", "Item Click Listener", message, grid);
    }

    private void createDoubleClickListener() {
        Div message = new Div();
        message.setId("doubleclicked-item");

        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.addItemDoubleClickListener(event -> message.setText(
                "Double Clicked Item: " + event.getItem().getFirstName()));

        grid.setId("item-doubleclick-listener");
        message.addClickListener(event -> message.setText(""));
        addCard("Click Listeners", "Item Double Click Listener", message, grid);
    }
}
