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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/dialog-with-combo")
public class DialogWithComboBoxPage extends Div {

    public DialogWithComboBoxPage() {
        Dialog dialog = new Dialog();

        ComboBox<String> combo = new ComboBox<>();
        combo.setItems("foo", "bar");
        combo.setId("combo");

        Div info = new Div();
        info.setId("info");

        combo.getElement().addPropertyChangeListener("opened",
                event -> info.setText(String.valueOf(combo.isOpened())));
        dialog.add(combo);

        NativeButton button = new NativeButton("Show dialog",
                event -> dialog.open());
        button.setId("open-dialog");

        add(info, button);
    }
}
