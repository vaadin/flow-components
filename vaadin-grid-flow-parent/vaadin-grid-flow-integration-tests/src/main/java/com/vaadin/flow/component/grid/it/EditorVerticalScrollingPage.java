/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/editor-vertical-scrolling")
public class EditorVerticalScrollingPage extends Div {

    private Grid<Person> grid = new Grid<>();

    @SuppressWarnings("deprecation")
    public EditorVerticalScrollingPage() {
        setSizeFull();
        grid.setSelectionMode(SelectionMode.NONE);
        grid.setId("editor-grid");
        grid.setItems(IntStream.range(1, 1001).mapToObj(this::createPerson)
                .collect(Collectors.toList()));
        Binder<Person> binder = new Binder<>(Person.class);
        TextField editorComponent = new TextField();
        editorComponent.setId("editor");
        binder.forField(editorComponent).bind("firstName");
        grid.addColumn(Person::getFirstName).setHeader("Name")
                .setEditorComponent(editorComponent);
        grid.addColumn(new ComponentRenderer<>(this::createComponent,
                this::editComponent));
        grid.getEditor().setBuffered(true).setBinder(binder);

        grid.getEditor().addOpenListener(
                event -> grid.setVerticalScrollingEnabled(false));
        grid.getEditor().addCloseListener(
                event -> grid.setVerticalScrollingEnabled(true));
        grid.setSizeFull();
        add(grid);
    }

    private Component editComponent(Component oldComponent, Person item) {
        return oldComponent;
    }

    private Person createPerson(int index) {
        Person person = new Person();
        person.setFirstName("Person " + index);
        person.setLastName(String.valueOf(index));
        return person;
    }

    private Component createComponent(Person item) {
        NativeButton save = new NativeButton("Save");
        NativeButton cancel = new NativeButton("Cancel");
        NativeButton edit = new NativeButton("Edit");
        save.addClickListener(event -> {
            if (grid.getEditor().save()) {
                save.setVisible(false);
                cancel.setVisible(false);
                edit.setVisible(true);
            }
        });
        cancel.addClickListener(event -> {
            grid.getEditor().cancel();
            save.setVisible(false);
            cancel.setVisible(false);
            edit.setVisible(true);
        });
        edit.addClickListener(event -> {
            if (grid.getEditor().isOpen()) {
                grid.getEditor().cancel();
            }
            grid.getEditor().editItem(item);
            save.setVisible(true);
            cancel.setVisible(true);
            edit.setVisible(false);
        });
        cancel.setVisible(false);
        save.setVisible(false);
        Div container = new Div(edit, save, cancel);
        edit.setId("edit-" + item.getLastName());
        save.setId("save-" + item.getLastName());
        cancel.setId("cancel-" + item.getLastName());
        return container;
    }

}
