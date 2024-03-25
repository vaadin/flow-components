/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/using-templates")
public class GridViewUsingTemplatesPage extends LegacyTestView {

    public GridViewUsingTemplatesPage() {
        createColumnTemplate();
    }

    private void createColumnTemplate() {
        List<Person> people = new ArrayList<>();
        people.addAll(createItems());

        Grid<Person> grid = new Grid<>();
        grid.setItems(people);

        // You can use the [[index]] variable to print the row index (0 based)
        grid.addColumn(TemplateRenderer.of("[[index]]")).setHeader("#");

        // You can set any property by using `withProperty`, including
        // properties not present on the original bean.
        grid.addColumn(TemplateRenderer.<Person> of(
                "<div title='[[item.firstName]]'>[[item.firstName]]<br><small>[[item.yearsOld]]</small></div>")
                .withProperty("firstName", Person::getFirstName)
                .withProperty("yearsOld",
                        person -> person.getAge() > 1
                                ? person.getAge() + " years old"
                                : person.getAge() + " year old"))
                .setHeader("Person");

        // You can also set complex objects directly. Internal properties of the
        // bean are accessible in the template.
        grid.addColumn(TemplateRenderer.<Person> of(
                "<div>[[item.address.street]], number [[item.address.number]]<br><small>[[item.address.postalCode]]</small></div>")
                .withProperty("address", Person::getAddress))
                .setHeader("Address");

        // You can set events handlers associated with the template. The syntax
        // follows the Polymer convention "on-event", such as "on-click".
        grid.addColumn(TemplateRenderer.<Person> of(
                "<button on-click='handleUpdate'>Update</button><button on-click='handleRemove'>Remove</button>")
                .withEventHandler("handleUpdate", person -> {
                    person.setFirstName(person.getFirstName() + " Updated");
                    grid.getDataProvider().refreshItem(person);
                }).withEventHandler("handleRemove", person -> {
                    ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                            .getDataProvider();
                    dataProvider.getItems().remove(person);
                    dataProvider.refreshAll();
                })).setHeader("Actions");

        grid.setSelectionMode(SelectionMode.NONE);
        grid.setId("template-renderer");
        addCard("Using templates", "Grid with columns using template renderer",
                grid);
    }
}
