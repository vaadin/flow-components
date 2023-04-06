/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.crud.CrudGrid;
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.crud.CrudI18nUpdatedEvent;
import com.vaadin.flow.component.crud.CrudVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.component.crud.examples.Helper.createPersonEditor;
import static com.vaadin.flow.component.crud.examples.Helper.createYorubaI18n;

@Route(value = "vaadin-crud")
public class MainView extends VerticalLayout {

    final VerticalLayout eventsPanel;
    final HorizontalLayout buttons;
    boolean hasBorder = true;

    public MainView() {
        eventsPanel = new VerticalLayout();
        buttons = new HorizontalLayout();
        eventsPanel.setId("events");

        final Crud<Person> crud = new Crud<>(Person.class,
                createPersonEditor());

        final Button newButton = new Button(
                CrudI18n.createDefault().getNewItem());
        newButton.setThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName());
        newButton.getElement().setAttribute("new-button", "");
        buttons.add(newButton);

        final Button serverSideNewButton = new Button(
                CrudI18n.createDefault().getNewItem());
        serverSideNewButton.setId("newServerItem");
        serverSideNewButton.addClickListener(
                e -> crud.edit(new Person(), Crud.EditMode.NEW_ITEM));
        buttons.add(serverSideNewButton);

        final Button serverSideEditButton = new Button(
                CrudI18n.createDefault().getEditItem());
        serverSideEditButton.setId("editServerItem");
        serverSideEditButton.addClickListener(e -> crud.edit(
                new Person(1, "Sayo", "Oladeji"), Crud.EditMode.EXISTING_ITEM));
        buttons.add(serverSideEditButton);

        final Span footer = new Span();
        crud.setToolbar(footer, newButton);

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        dataProvider.setSizeChangeListener(count -> footer
                .setText(String.format("%d items available", count)));

        crud.setDataProvider(dataProvider);

        final Button showFiltersButton = new Button("Show filter");
        showFiltersButton.setId("showFilter");
        showFiltersButton.addClickListener(event -> {
            CrudGrid<Person> grid = (CrudGrid<Person>) crud.getGrid();
            CrudFilter filter = grid.getFilter();
            String filterString = filter.getConstraints().toString()
                    + filter.getSortOrders().toString();

            addEvent(filterString);
        });
        buttons.add(showFiltersButton);

        final Button updateI18nButton = new Button("Switch to Yoruba",
                event -> {
                    CrudI18n yorubaI18n = createYorubaI18n();
                    crud.setI18n(yorubaI18n);
                    newButton.setText(yorubaI18n.getNewItem());
                });
        updateI18nButton.setId("updateI18n");
        buttons.add(updateI18nButton);

        ComponentUtil.addListener(crud.getGrid(), CrudI18nUpdatedEvent.class,
                e -> addEvent("I18n updated"));
        buttons.add(updateI18nButton);

        // no-border should be reflected to the generated grid too
        final Button toggleBordersButton = new Button("Toggle borders",
                event -> {
                    if (hasBorder) {
                        crud.addThemeVariants(CrudVariant.NO_BORDER);
                    } else {
                        crud.removeThemeVariants(CrudVariant.NO_BORDER);
                    }
                    hasBorder = !hasBorder;
                });
        toggleBordersButton.setId("toggleBorders");
        buttons.add(toggleBordersButton);

        final Button addGridButton = new Button("Add grid", event -> {
            Grid<Person> grid = new Grid<>(Person.class);
            grid.addColumn(Person::getId).setHeader("Id");
            grid.addColumn(Person::getFirstName).setHeader("First Name");
            grid.addColumn(Person::getLastName).setHeader("Last Name");
            crud.setGrid(grid);
        });
        addGridButton.setId("addGrid");
        buttons.add(addGridButton);

        final Button addNewEventListener = new Button("Add New-Event listener",
                event -> {
                    crud.addNewListener(e -> {
                        Person item = e.getItem();
                        item.setId(0);
                        item.setFirstName("firstName");
                        item.setLastName("lastName");
                        crud.getEditor().setItem(e.getItem());
                    });
                });
        addNewEventListener.setId("newEventListener");

        buttons.add(addNewEventListener);

        final Button showToolbarButton = new Button("Show toolbar", event -> {
            crud.setToolbarVisible(true);
        });
        showToolbarButton.setId("showToolbarButton");
        buttons.add(showToolbarButton);

        final Button hideToolbarButton = new Button("Hide toolbar", event -> {
            crud.setToolbarVisible(false);
        });
        hideToolbarButton.setId("hideToolbarButton");
        buttons.add(hideToolbarButton);

        crud.addNewListener(e -> addEvent("New: " + e.getItem()));
        crud.addEditListener(e -> addEvent("Edit: " + e.getItem()));
        crud.addCancelListener(e -> addEvent("Cancel: " + e.getItem()));

        crud.addDeleteListener(e -> addEvent("Delete: " + e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.addSaveListener(e -> addEvent("Save: " + e.getItem()));
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));

        setHeight("100%");
        add(crud, buttons, eventsPanel);
    }

    private void addEvent(String event) {
        eventsPanel.add(new Span(event));
    }
}
