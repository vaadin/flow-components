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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("combobox-navigation")
public class ComboBoxNavigationView extends Div {

    public ComboBoxNavigationView() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("One", "Two", "Three", "Four");
        comboBox.addValueChangeListener(event -> {
            UI.getCurrent().navigate("combobox-navigation-out");
        });

        add(comboBox);
    }

    @Route("combobox-navigation-out")
    public static class DummyView extends Div {
        public DummyView() {
            setId("dummy-view");
            setText("Nothing here");
        }
    }
}
