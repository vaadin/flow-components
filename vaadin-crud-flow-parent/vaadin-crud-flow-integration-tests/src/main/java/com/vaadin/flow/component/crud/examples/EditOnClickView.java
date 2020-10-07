package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditorPosition;
import com.vaadin.flow.component.crud.CrudGrid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route(value = "vaadin-crud/editonclick")
@Theme(Lumo.class)
public class EditOnClickView extends VerticalLayout {
    public static String CLICKTOEDIT_BUTTON_ID =  "setClickToEdit";

    public EditOnClickView() {
        final CrudGrid<Person> grid = new CrudGrid<>(Person.class, false);
        final Crud<Person> crud = new Crud<>(Person.class, grid,
                Helper.createPersonEditor());

        PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();

        grid.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.setEditorPosition(CrudEditorPosition.ASIDE);

        final Button setClickToEdit = new Button("Set Click Row to Edit",
                event -> crud.setEditOnClick(true));
        setClickToEdit.setId(CLICKTOEDIT_BUTTON_ID);

        setHeight("100%");
        add(crud, setClickToEdit);
    }
}
