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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/dialog-class-names-test")
public class DialogWithClassNamesPage extends Div {

    private Dialog dialog;

    public DialogWithClassNamesPage() {
        dialog = new Dialog();
        dialog.addClassName("custom");

        NativeButton addClass = new NativeButton("Add class",
                event -> dialog.addClassName("added"));
        addClass.setId("add");
        dialog.add(addClass);

        NativeButton clearAllClass = new NativeButton("Clear all class",
                event -> dialog.getClassNames().clear());
        clearAllClass.setId("clear");
        dialog.add(clearAllClass);

        NativeButton open = new NativeButton("Open dialog",
                event -> dialog.open());
        open.setId("open");
        add(open);
    }

}
