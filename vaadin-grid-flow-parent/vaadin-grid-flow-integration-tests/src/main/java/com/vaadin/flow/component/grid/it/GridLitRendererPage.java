/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/lit-renderer")
public class GridLitRendererPage extends Div {

    public GridLitRendererPage() {
        Grid<Integer> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, 1000).boxed().collect(Collectors.toList()));
        grid.addColumn(LitRenderer
                .<Integer> of(
                        "<span id=\"item-${index}\">Lit: ${item.name}</span>")
                .withProperty("name", item -> "Item " + item))
                .setEditorComponent(new Span("Editor component"));
        grid.getEditor().setBinder(new Binder<>());
        setLitRenderer(grid);
        grid.setId("lit-renderer-grid");
        add(grid);

        NativeButton componentRendererButton = new NativeButton(
                "Set ComponentRenderer", e -> {
                    setComponentRenderer(grid);
                });
        componentRendererButton.setId("componentRendererButton");

        NativeButton litRendererButton = new NativeButton("Set LitRenderer",
                e -> {
                    setLitRenderer(grid);
                });
        litRendererButton.setId("litRendererButton");

        NativeButton toggleEditButton = new NativeButton("Toggle edit mode",
                e -> {
                    if (grid.getEditor().isOpen()) {
                        grid.getEditor().cancel();
                    } else {
                        grid.getEditor().editItem(0);
                    }
                });
        toggleEditButton.setId("toggleEditButton");

        NativeButton toggleAttachedButton = new NativeButton("Toggle attached",
                e -> {
                    if (grid.isAttached()) {
                        remove(grid);
                    } else {
                        add(grid);
                    }
                });
        toggleAttachedButton.setId("toggleAttachedButton");

        add(componentRendererButton, litRendererButton, toggleEditButton,
                toggleAttachedButton);

        createLitRendererColumns();
    }

    private void createLitRendererColumns() {
        // Only the first rows are exercised by the tests, so a small data set
        // keeps the page light
        List<Person> people = new ArrayList<>(
                new PeopleGenerator().generatePeople(50));

        Grid<Person> grid = new Grid<>();
        grid.setItems(people);

        // You can use the index variable to print the row index (0 based)
        grid.addColumn(LitRenderer.of("${index}")).setHeader("#");

        // You can set any property by using `withProperty`, including
        // properties not present on the original bean.
        grid.addColumn(LitRenderer.<Person> of(
                "<div title='${item.firstName}'>${item.firstName}<br><small>${item.yearsOld}</small></div>")
                .withProperty("firstName", Person::getFirstName)
                .withProperty("yearsOld",
                        person -> person.getAge() > 1
                                ? person.getAge() + " years old"
                                : person.getAge() + " year old"))
                .setHeader("Person");

        // You can also set complex objects directly. Internal properties of the
        // bean are accessible in the template.
        grid.addColumn(LitRenderer.<Person> of(
                "<div>${item.address.street}, number ${item.address.number}<br><small>${item.address.postalCode}</small></div>")
                .withProperty("address", Person::getAddress))
                .setHeader("Address");

        // You can set events handlers associated with the template. The syntax
        // follows the Polymer convention "on-event", such as "on-click".
        grid.addColumn(LitRenderer.<Person> of(
                "<button @click=${handleUpdate}>Update</button><button @click=${handleRemove}>Remove</button>")
                .withFunction("handleUpdate", person -> {
                    person.setFirstName(person.getFirstName() + " Updated");
                    grid.getDataProvider().refreshItem(person);
                }).withFunction("handleRemove", person -> {
                    ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                            .getDataProvider();
                    dataProvider.getItems().remove(person);
                    dataProvider.refreshAll();
                })).setHeader("Actions");

        grid.setSelectionMode(SelectionMode.NONE);
        grid.setId("template-renderer");
        add(grid);
    }

    private void setLitRenderer(Grid<Integer> grid) {
        grid.setItemDetailsRenderer(LitRenderer.<Integer> of(
                "<span id=\"details-${index}\">Lit: ${item.name}</span>")
                .withProperty("name", item -> "Item details " + item));
    }

    private void setComponentRenderer(Grid<Integer> grid) {
        grid.setItemDetailsRenderer(
                new ComponentRenderer<Component, Integer>(item -> {
                    Div content = new Div();
                    content.setId("details-" + item);
                    content.setText("Component: Item details " + item);
                    return content;
                }));
    }

}
