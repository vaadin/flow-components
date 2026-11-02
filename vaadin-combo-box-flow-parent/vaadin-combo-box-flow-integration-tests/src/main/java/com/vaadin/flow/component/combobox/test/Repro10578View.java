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
package com.vaadin.flow.component.combobox.test;

import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("repro-10578")
public class Repro10578View extends Div {

    private static final List<String> ALL = List.of(" ", "Option A",
            "Option B");

    public Repro10578View() {
        // Failing case: only current value up front, full list on focus
        ComboBox<String> combo = new ComboBox<>("Lazy on focus");
        combo.setId("lazy");
        combo.setItems(List.of(" "));
        combo.setValue(" ");
        Span lazyValue = new Span("-");
        lazyValue.setId("lazy-value");
        combo.addFocusListener(e -> {
            String previous = combo.getValue();
            combo.setItems(ALL);
            if (ALL.contains(previous)) {
                combo.setValue(previous);
            }
        });
        combo.addValueChangeListener(e -> lazyValue.setText(
                "[" + e.getValue() + "] fromClient=" + e.isFromClient()));

        // Control: full list up front, no focus listener
        ComboBox<String> control = new ComboBox<>("Control");
        control.setId("control");
        control.setItems(ALL);
        control.setValue(" ");
        Span controlValue = new Span("-");
        controlValue.setId("control-value");
        control.addValueChangeListener(e -> controlValue.setText(
                "[" + e.getValue() + "] fromClient=" + e.isFromClient()));

        NativeButton reset = new NativeButton("Reset lazy", e -> {
            combo.setItems(List.of(" "));
            combo.setValue(" ");
            lazyValue.setText("-");
        });
        reset.setId("reset");

        add(new Div(combo, lazyValue), new Div(control, controlValue), reset);
    }
}
