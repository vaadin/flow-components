/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditorPosition;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-crud/editorposition")
public class EditorPositionView extends VerticalLayout {

    public EditorPositionView() {
        final Grid<Person> grid = new Grid<>(Person.class);
        final Crud<Person> crud = new Crud<>(Person.class, grid,
                Helper.createPersonEditor());

        PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();

        grid.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        Crud.addEditColumn(grid);

        final Button positionBottom = new Button("Set Editor Position Bottom",
                event -> crud.setEditorPosition(CrudEditorPosition.BOTTOM));
        positionBottom.setId("positionBottom");

        final Button positionAside = new Button("Set Editor Position Aside",
                event -> crud.setEditorPosition(CrudEditorPosition.ASIDE));
        positionAside.setId("positionAside");

        final Button positionOverlay = new Button("Set Editor Position Overlay",
                event -> crud.setEditorPosition(CrudEditorPosition.OVERLAY));
        positionOverlay.setId("positionOverlay");

        Span editorPosition = new Span();
        editorPosition.setId("editorPositionLabel");

        final Button getEditorPosition = new Button("Get Editor Position",
                event -> editorPosition.setText(
                        crud.getElement().getProperty("editorPosition")));
        getEditorPosition.setId("getEditorPosition");

        setHeight("100%");
        add(crud, positionBottom, positionAside, positionOverlay);
        add(editorPosition, getEditorPosition);
    }
}
