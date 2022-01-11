/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
