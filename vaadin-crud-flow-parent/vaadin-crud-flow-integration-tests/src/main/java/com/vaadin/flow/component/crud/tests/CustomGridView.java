package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-crud/customgrid")
public class CustomGridView extends VerticalLayout {

    boolean hasBorder = true;

    public CustomGridView() {
        final Grid<Person> grid = new Grid<>(Person.class);
        final Crud<Person> crud = new Crud<>(Person.class, grid,
                Helper.createPersonEditor());

        PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();

        grid.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        Crud.addEditColumn(grid);

        // no-border should not be reflected to the custom grid
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

        final Button customGridClickToEditButton = new Button(
                "Set click row to open", event -> {
                    grid.addItemClickListener(listener -> crud.edit(
                            listener.getItem(), Crud.EditMode.EXISTING_ITEM));
                });
        customGridClickToEditButton.setId("clickToEdit");

        final Button openNewItemButton = new Button("Open new item editor",
                event -> crud.edit(new Person(), Crud.EditMode.NEW_ITEM));
        openNewItemButton.setId("newItemEditor");

        setHeight("100%");
        add(crud, toggleBordersButton, customGridClickToEditButton,
                openNewItemButton);
    }
}
