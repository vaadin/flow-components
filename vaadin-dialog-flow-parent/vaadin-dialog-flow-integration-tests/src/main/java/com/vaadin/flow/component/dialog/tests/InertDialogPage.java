/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/inert-dialog")
public class InertDialogPage extends Div {
    public InertDialogPage() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        add(dialog);

        NativeButton setInert = new NativeButton("Set inert", event -> {
            ElementUtil.setInert(dialog.getElement(), true);
        });
        setInert.setId("set-inert");
        dialog.add(setInert);

        dialog.open();
    }
}
