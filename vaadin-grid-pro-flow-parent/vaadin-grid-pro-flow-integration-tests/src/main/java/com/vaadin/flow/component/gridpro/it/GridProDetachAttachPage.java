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
