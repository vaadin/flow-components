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
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1546.
 *
 * setValue with a value not present in the ComboBox items (allowCustomValue
 * false) reportedly sets that value on the server and shows it in the input
 * field, rather than throwing or being ignored. Explores whether the data
 * provider being in-memory vs lazy makes a difference, and the multi-select
 * variant.
 */
@Route("repro-1546")
public class Repro1546View extends Div {

    public Repro1546View() {
        // in-memory single select, custom values disallowed
        ComboBox<String> inMemory = new ComboBox<>();
        inMemory.getElement().setAttribute("id", "in-memory");
        inMemory.setItems("value_2", "value_3");
        inMemory.setAllowCustomValue(false);
        Span inMemoryLog = log("in-memory-log");
        NativeButton setInMemory = new NativeButton("in-memory: setValue(value_1)",
                e -> {
                    inMemory.setValue("value_1");
                    inMemoryLog.setText("getValue()=" + inMemory.getValue());
                });
        setInMemory.setId("set-in-memory");

        // lazy (callback) provider, custom values disallowed
        ComboBox<String> lazy = new ComboBox<>();
        lazy.getElement().setAttribute("id", "lazy");
        lazy.setItems(query -> List.of("value_2", "value_3").stream()
                .skip(query.getOffset()).limit(query.getLimit()));
        lazy.setAllowCustomValue(false);
        Span lazyLog = log("lazy-log");
        NativeButton setLazy = new NativeButton("lazy: setValue(value_1)", e -> {
            lazy.setValue("value_1");
            lazyLog.setText("getValue()=" + lazy.getValue());
        });
        setLazy.setId("set-lazy");

        // multi select, custom values disallowed
        MultiSelectComboBox<String> multi = new MultiSelectComboBox<>();
        multi.getElement().setAttribute("id", "multi");
        multi.setItems("value_2", "value_3");
        multi.setAllowCustomValue(false);
        Span multiLog = log("multi-log");
        NativeButton setMulti = new NativeButton("multi: setValue(value_1)",
                e -> {
                    multi.setValue(java.util.Set.of("value_1"));
                    multiLog.setText("getValue()=" + multi.getValue());
                });
        setMulti.setId("set-multi");

        add(section("In-memory single select", inMemory, setInMemory,
                inMemoryLog),
                section("Lazy single select", lazy, setLazy, lazyLog),
                section("Multi select", multi, setMulti, multiLog));
    }

    private static Span log(String id) {
        Span span = new Span("getValue()=null");
        span.setId(id);
        return span;
    }

    private static Div section(String title,
            com.vaadin.flow.component.Component... children) {
        Div div = new Div();
        div.add(new Span(title));
        div.add(children);
        div.getStyle().set("margin-bottom", "1em");
        return div;
    }
}
