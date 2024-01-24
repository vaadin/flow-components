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
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/client-side-data-range")
public class ComboBoxClientSideDataRangePage extends Div {
    public static final int ITEMS_COUNT = 900;

    public ComboBoxClientSideDataRangePage() {
        ComboBox<String> comboBox = new ComboBox<>();
        List<String> items = new ArrayList<>();
        for (int i = 0; i < ITEMS_COUNT; i++) {
            items.add("Item " + i);
        }
        comboBox.setItems(items);

        Input setPageSizeInput = new Input();
        setPageSizeInput.setId("set-page-size");
        setPageSizeInput.setPlaceholder("Set page size");
        setPageSizeInput.addValueChangeListener(event -> {
            int pageSize = Integer.parseInt(event.getValue());
            comboBox.setPageSize(pageSize);
        });

        add(comboBox, setPageSizeInput);
    }
}
