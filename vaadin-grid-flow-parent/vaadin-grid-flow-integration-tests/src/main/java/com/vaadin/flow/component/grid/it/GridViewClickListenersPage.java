/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        grid.addColumn(new ComponentRenderer<>(person -> {
            return new Button(VaadinIcon.PENCIL.create());
        })).setHeader("Button");

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
