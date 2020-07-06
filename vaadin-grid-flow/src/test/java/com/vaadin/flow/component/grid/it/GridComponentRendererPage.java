/*
 * Copyright 2000-2019 Vaadin Ltd.
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

package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test view that adds rows with components to a Grid.
 *
 * Page that reproduces the bug described at
 * https://github.com/vaadin/vaadin-grid-flow/issues/557
 *
 */
@Route("grid-component-renderer")
public class GridComponentRendererPage extends Div {

    int index = 1;

    public GridComponentRendererPage() {
        final List<String> items = new ArrayList<>();

        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.setItems(items);

        SerializableBiConsumer<TextField, String> itemTextFieldConsumer =(text, val) -> {
            text.setId("vaadin-text-field-" +index);
            index++;
        };

        SerializableBiConsumer<ComboBox<String>, String> itemComboBoxConsumer = (comboBox, val) -> {
            comboBox.setId("vaadin-combo-box-" + index);
            index++;
        };

        grid.addColumn(new ComponentRenderer<>(TextField::new, itemTextFieldConsumer)).setHeader("Header 1");
        grid.addColumn(new ComponentRenderer<>(ComboBox<String>::new, itemComboBoxConsumer)).setHeader("Header 2");
        grid.addColumn(new ComponentRenderer<>(ComboBox<String>::new, itemComboBoxConsumer)).setHeader("Header 3");
        grid.addColumn(new ComponentRenderer<>(ComboBox<String>::new, itemComboBoxConsumer)).setHeader("Header 4");
        grid.addColumn(new ComponentRenderer<>(ComboBox<String>::new, itemComboBoxConsumer)).setHeader("Header 5");

        NativeButton addRowButton = new NativeButton("add row", event -> {
            items.add(String.valueOf(index));
            index++;
            grid.getDataProvider().refreshAll();
        });

        addRowButton.setId("add-row-button");

        add(addRowButton, grid);

    }
}
