/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.tests;

import java.util.List;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route(value = "gridpro-item-details")
public class GridProItemDetailsPage extends Div {

    public GridProItemDetailsPage() {
        List<SamplePerson> persons = List.of(new SamplePerson("Henry"),
                new SamplePerson("Indiana"), new SamplePerson("Jones"));

        GridPro<SamplePerson> grid = new GridPro<>();
        grid.addColumn(SamplePerson::getName).setHeader("Readonly Name");
        grid.addEditColumn(SamplePerson::getName)
                .custom(new TextField(), SamplePerson::setName)
                .setHeader("Editable Name");
        grid.setItemDetailsRenderer(new TextRenderer<>(
                person -> "Details for " + person.getName()));
        grid.setItems(persons);

        add(grid);
    }

    public static class SamplePerson {
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
