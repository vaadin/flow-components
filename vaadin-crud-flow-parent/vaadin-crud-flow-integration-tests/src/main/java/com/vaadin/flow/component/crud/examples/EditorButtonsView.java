/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditorPosition;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.component.crud.examples.Helper.createPersonEditor;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;

@Route(value = "vaadin-crud/editorbuttons")
public class EditorButtonsView extends VerticalLayout {

    final HorizontalLayout buttonsLayout;

    public EditorButtonsView() {
        final Crud<Person> crud = new Crud<>(Person.class,
                createPersonEditor());
        crud.setEditorPosition(CrudEditorPosition.ASIDE);

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));

        buttonsLayout = new HorizontalLayout();

        final Button disableSaveButton = new Button("Disable save button",
                event -> {
                    Button saveButton = crud.getSaveButton();
                    saveButton.setEnabled(false);
                });
        disableSaveButton.setId("disable-save-button");

        final Button enableSaveButton = new Button("Enable save button",
                event -> {
                    Button saveButton = crud.getSaveButton();
                    saveButton.setEnabled(true);
                });
        enableSaveButton.setId("enable-save-button");

        final Button disableCancelButton = new Button("Disable cancel button",
                event -> {
                    Button cancelButton = crud.getCancelButton();
                    cancelButton.setEnabled(false);
                });
        disableCancelButton.setId("disable-cancel-button");

        final Button enableCancelButton = new Button("Enable cancel button",
                event -> {
                    Button cancelButton = crud.getCancelButton();
                    cancelButton.setEnabled(true);
                });
        enableCancelButton.setId("enable-cancel-button");

        final Button disableDeleteButton = new Button("Disable delete button",
                event -> {
                    Button deleteButton = crud.getDeleteButton();
                    deleteButton.setEnabled(false);
                });
        disableDeleteButton.setId("disable-delete-button");

        final Button enableDeleteButton = new Button("Enable delete button",
                event -> {
                    Button deleteButton = crud.getDeleteButton();
                    deleteButton.setEnabled(true);
                });
        enableDeleteButton.setId("enable-delete-button");

        final Button addEnterShortcut = new Button(
                "Add Enter shortcut for save", event -> {
                    Button saveButton = crud.getSaveButton();
                    saveButton.addClickShortcut(Key.ENTER);
                    saveButton.addClickListener(e -> {
                        if (!e.isFromClient()) {
                            saveButton.getElement().executeJs("this.click()");
                        }
                    });
                    saveButton.addFocusShortcut(Key.KEY_F, KeyModifier.ALT);
                });
        addEnterShortcut.setId("add-enter-shortcut-button");

        buttonsLayout.add(disableSaveButton, enableSaveButton,
                disableCancelButton, enableCancelButton, disableDeleteButton,
                enableDeleteButton, addEnterShortcut);

        setHeight("100%");
        add(crud, buttonsLayout);
    }
}
