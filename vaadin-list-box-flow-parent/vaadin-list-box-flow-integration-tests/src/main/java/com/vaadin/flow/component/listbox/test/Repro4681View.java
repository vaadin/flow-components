/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox.test;

import java.util.stream.Collectors;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/4681
 *
 * Calling removeItem/addItem (on a NON-selected item) inside a
 * ValueChangeListener clears the current selection.
 */
@Route("repro-4681")
public class Repro4681View extends Div {

    public Repro4681View() {
        // --- Case A: single-select ListBox (reporter: buggy) ---
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Hans", "Franz", "Dorothee");
        Span listBoxValue = logSpan("listbox-value");
        listBox.addValueChangeListener(e -> {
            var dv = listBox.getListDataView();
            dv.removeItem("Dorothee");
            dv.addItem("Dorothee");
            listBoxValue.setText(String.valueOf(listBox.getValue()));
        });
        listBox.setId("listbox");

        // --- Case B: MultiSelectListBox, default mode DISCARD (buggy) ---
        MultiSelectListBox<String> msDefault = new MultiSelectListBox<>();
        msDefault.setItems("Hans", "Franz", "Dorothee");
        Span msDefaultValue = logSpan("ms-default-value");
        msDefault.addValueChangeListener(e -> {
            var dv = msDefault.getListDataView();
            dv.removeItem("Dorothee");
            dv.addItem("Dorothee");
            msDefaultValue.setText(selection(msDefault));
        });
        msDefault.setId("ms-default");

        // --- Case C: MultiSelectListBox, PRESERVE_EXISTING (control/fix) ---
        MultiSelectListBox<String> msPreserve = new MultiSelectListBox<>();
        msPreserve.setItems("Hans", "Franz", "Dorothee");
        msPreserve.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);
        Span msPreserveValue = logSpan("ms-preserve-value");
        msPreserve.addValueChangeListener(e -> {
            var dv = msPreserve.getListDataView();
            dv.removeItem("Dorothee");
            dv.addItem("Dorothee");
            msPreserveValue.setText(selection(msPreserve));
        });
        msPreserve.setId("ms-preserve");

        add(new Div(new Span("A) ListBox (single) — click Hans:"), listBox,
                listBoxValue));
        add(new Div(new Span("B) MultiSelectListBox DISCARD — click Hans:"),
                msDefault, msDefaultValue));
        add(new Div(
                new Span("C) MultiSelectListBox PRESERVE_EXISTING — click Hans:"),
                msPreserve, msPreserveValue));
    }

    private static String selection(MultiSelectListBox<String> box) {
        return box.getSelectedItems().stream().sorted()
                .collect(Collectors.joining(","));
    }

    private static Span logSpan(String id) {
        Span span = new Span("<none>");
        span.setId(id);
        return span;
    }
}
