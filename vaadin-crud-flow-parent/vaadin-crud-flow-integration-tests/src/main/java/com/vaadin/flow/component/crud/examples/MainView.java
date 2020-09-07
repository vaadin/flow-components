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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import static com.vaadin.flow.component.crud.examples.Helper.createPersonEditor;
import static com.vaadin.flow.component.crud.examples.Helper.createYorubaI18n;

@Route(value = "vaadin-crud")
@Theme(Lumo.class)
public class MainView extends VerticalLayout {

    final VerticalLayout eventsPanel;
    boolean hasBorder = true;

    public MainView() {
        eventsPanel = new VerticalLayout();
        eventsPanel.setId("events");

        final Crud<Person> crud = new Crud<>(Person.class, createPersonEditor());

        final Button newButton = new Button(CrudI18n.createDefault().getNewItem());
        newButton.setThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName());
        newButton.getElement().setAttribute("new-button", "");

        final Button serverSideNewButton = new Button(CrudI18n.createDefault().getNewItem());
        serverSideNewButton.setId("newServerItem");
        serverSideNewButton.addClickListener(e -> crud.edit(new Person(), Crud.EditMode.NEW_ITEM));

        final Button serverSideEditButton = new Button(CrudI18n.createDefault().getEditItem());
        serverSideEditButton.setId("editServerItem");
        serverSideEditButton.addClickListener(e ->
                crud.edit(new Person(1, "Sayo", "Oladeji"), Crud.EditMode.EXISTING_ITEM));

        final Span footer = new Span();
        crud.setToolbar(footer, newButton);

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        dataProvider.setSizeChangeListener(count ->
                footer.setText(String.format("%d items available", count)));

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

        final Button updateI18nButton = new Button("Switch to Yoruba", event -> {
            CrudI18n yorubaI18n = createYorubaI18n();
            crud.setI18n(yorubaI18n);
            newButton.setText(yorubaI18n.getNewItem());
        });
        updateI18nButton.setId("updateI18n");
        ComponentUtil.addListener(crud.getGrid(), CrudI18nUpdatedEvent.class,
                e -> addEvent("I18n updated"));

        // no-border should be reflected to the generated grid too
        final Button toggleBordersButton = new Button("Toggle borders", event -> {
            if (hasBorder) {
                crud.addThemeVariants(CrudVariant.NO_BORDER);
            } else {
                crud.removeThemeVariants(CrudVariant.NO_BORDER);
            }
            hasBorder = !hasBorder;
        });
        toggleBordersButton.setId("toggleBorders");

        crud.addNewListener(e -> addEvent("New: " + e.getItem()));
        crud.addEditListener(e -> addEvent("Edit: " + e.getItem()));
        crud.addCancelListener(e -> addEvent("Cancel: " + e.getItem()));

        crud.addDeleteListener(e -> addEvent("Delete: " + e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.addSaveListener(e -> addEvent("Save: " + e.getItem()));
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));

        setHeight("100%");
        add(crud, serverSideNewButton, serverSideEditButton, showFiltersButton,
                updateI18nButton, toggleBordersButton, eventsPanel);
    }

    private void addEvent(String event) {
        eventsPanel.add(new Span(event));
    }
}
