/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("pre-selected")
public class PreSelectedValuePage extends Div {

    private static final String PRE_SELECTED_VALUE = "Item 1";

    public PreSelectedValuePage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(IntStream.range(0, 20).mapToObj(i -> "Item " + i));
        comboBox.setValue(PRE_SELECTED_VALUE);
        comboBox.setId("combo");

        Div div = new Div();
        div.setId("info");

        NativeButton button = new NativeButton("Print combo-box value",
                event -> div.setText(comboBox.getValue()));
        button.setId("get-value");

        add(button, div, comboBox);

    }
}
