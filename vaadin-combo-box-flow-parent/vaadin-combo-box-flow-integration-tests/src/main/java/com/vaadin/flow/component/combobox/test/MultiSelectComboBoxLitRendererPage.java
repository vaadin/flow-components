/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
