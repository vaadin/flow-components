/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route(value = "gridpro-detach-attach")
public class GridProDetachAttachPage extends Div {

    public GridProDetachAttachPage() {
        List<SamplePerson> persons = new ArrayList<>();

        persons.add(new SamplePerson("Henry"));
        persons.add(new SamplePerson("Indiana"));
        persons.add(new SamplePerson("Jones"));

        GridPro<SamplePerson> grid = new GridPro<>();
        grid.setId("grid-pro");

        TextField field = new TextField();

        grid.addEditColumn(SamplePerson::getName).custom(field,
                SamplePerson::setName);
        grid.setItems(persons);

        NativeButton attachDetachButton = new NativeButton("Detach", event -> {
            if (grid.getParent().isPresent()) {
                remove(grid);
                event.getSource().setText("Reattach");
            } else {
                add(grid);
                event.getSource().setText("Detach");
            }
        });
        attachDetachButton.setId("toggle-attached");

        NativeButton addColumnButton = new NativeButton("Add new Column",
                event -> {
                    TextField newEditColumn = new TextField();
                    grid.addEditColumn(SamplePerson::getName)
                            .custom(newEditColumn, SamplePerson::setName);
                });
        addColumnButton.setId("add-column");

        add(grid, attachDetachButton, addColumnButton);
    }

    public class SamplePerson {
        private String name;

        public SamplePerson(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
