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
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/string-items-text-renderer")
public class StringItemsWithTextRendererPage extends Div {

    public StringItemsWithTextRendererPage() {
        Div info = new Div();
        info.setId("info");
        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.setRenderer(new TextRenderer<>());
        comboBox.setItems("foo", "bar");
        comboBox.setId("list");
        comboBox.addValueChangeListener(
                event -> info.setText(comboBox.getValue()));

        add(comboBox, info);
    }
}
