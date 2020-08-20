package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditorPosition;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route
@Theme(Lumo.class)
@BodySize(height = "100vh", width = "100vw")
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

        Label editorPosition = new Label();
        editorPosition.setId("editorPositionLabel");

        final Button getEditorPosition = new Button("Get Editor Position",
                event -> editorPosition.setText(crud.getElement().getProperty("editorPosition")));
        getEditorPosition.setId("getEditorPosition");

        setHeight("100%");
        add(crud, positionBottom, positionAside, positionOverlay);
        add(editorPosition, getEditorPosition);
    }
}
