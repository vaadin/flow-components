/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/lit-renderer")
public class MultiSelectComboBoxLitRendererPage extends Div {

    public MultiSelectComboBoxLitRendererPage() {
        addReadOnlyUseCase();
    }

    private void addReadOnlyUseCase() {
        MultiSelectComboBox<Integer> multiSelect = new MultiSelectComboBox<>();
        multiSelect.setId("read-only-multi-select-combo-box");
        multiSelect.setItems(
                IntStream.range(0, 1000).boxed().collect(Collectors.toList()));
        multiSelect.select(0, 1);
        multiSelect.setRenderer(createLitRenderer());
        multiSelect.setReadOnly(true);
        add(multiSelect);
    }

    private LitRenderer<Integer> createLitRenderer() {
        return LitRenderer
                .<Integer> of(
                        "<div id=\"item-${index}\">Lit: ${item.name}</div>")
                .withProperty("name", item -> "Item " + item);
    }

}
