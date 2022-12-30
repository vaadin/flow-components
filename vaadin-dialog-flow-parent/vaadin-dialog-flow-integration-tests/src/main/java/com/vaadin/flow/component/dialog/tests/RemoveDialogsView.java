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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "remove-dialogs-view")
public class RemoveDialogsView extends Div {

    protected void onAttach(AttachEvent event) {
        NativeButton button = new NativeButton("Close all", e -> {
            Dialog.closeAllDialogs();
        });

        Dialog dialog1 = new Dialog();
        dialog1.add(new Span("Dialog 1"));

        Dialog dialog2 = new Dialog();
        dialog2.add(new Span("Dialog 2"));
        dialog2.add(button);

        dialog1.open();
        dialog2.open();
    }
}
