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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/lit-renderer")
public class ComboBoxLitRendererPage extends Div {

    public ComboBoxLitRendererPage() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.setItems(
                IntStream.range(0, 1000).boxed().collect(Collectors.toList()));
        setLitRenderer(combo);
        add(combo);

        NativeButton componentRendererButton = new NativeButton(
                "Set ComponentRenderer", e -> {
                    setComponentRenderer(combo);
                });
        componentRendererButton.setId("componentRendererButton");

        NativeButton litRendererButton = new NativeButton("Set LitRenderer",
                e -> {
                    setLitRenderer(combo);
                });
        litRendererButton.setId("litRendererButton");
        add(componentRendererButton, litRendererButton);
    }

    private void setLitRenderer(ComboBox<Integer> combo) {
        combo.setRenderer(LitRenderer
                .<Integer> of(
                        "<div id=\"item-${index}\">Lit: ${item.name}</div>")
                .withProperty("name", item -> "Item " + item));
    }

    private void setComponentRenderer(ComboBox<Integer> combo) {
        combo.setRenderer(new ComponentRenderer<Component, Integer>(item -> {
            Div content = new Div();
            content.setId("item-" + item);
            content.setText("Component: Item " + item);
            return content;
        }));
    }

}
