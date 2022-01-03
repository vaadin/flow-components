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
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/detach-reattach")
public class DetachReattachPage extends Div {

    public DetachReattachPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("foo", "bar");

        NativeButton detach = new NativeButton("detach", e -> remove(comboBox));
        detach.setId("detach");

        NativeButton attach = new NativeButton("attach", e -> add(comboBox));
        attach.setId("attach");

        NativeButton attachDetach = new NativeButton("attach-detach", e -> {
            add(comboBox);
            remove(comboBox);
        });
        attachDetach.setId("attach-detach");

        NativeButton setValue = new NativeButton("set value foo",
                e -> comboBox.setValue("foo"));
        setValue.setId("set-value");

        Div valueChanges = new Div();
        valueChanges.setId("value-changes");
        comboBox.addValueChangeListener(e -> {
            valueChanges.add(new Paragraph(e.getValue()));
        });

        add(comboBox, detach, attach, attachDetach, setValue, valueChanges);
    }
}
