/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud.examples;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import static com.vaadin.flow.component.crud.examples.Helper.createPersonEditor;

@Route("vaadin-crud/detach-attach")
public class DetachAttachView extends Div {

    public DetachAttachView() {
        final Crud<Person> crud = new Crud<>(Person.class,
                createPersonEditor());

        final PersonCrudDataProvider dataProvider = new PersonCrudDataProvider();
        crud.setDataProvider(dataProvider);

        NativeButton detach = new NativeButton("detach", e -> remove(crud));
        detach.setId("detach");
        NativeButton attach = new NativeButton("attach", e -> add(crud));
        attach.setId("attach");

        NativeButton disableSaveBtn = new NativeButton("disable save button",
                e -> crud.getSaveButton().setEnabled(false));
        disableSaveBtn.setId("disable-save-button");

        add(crud, detach, attach, disableSaveBtn);
    }

}
