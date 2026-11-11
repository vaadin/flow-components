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
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.ItemCountChangeEvent;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/repro-7650")
public class Repro7650View extends Div {

    public Repro7650View() {
        // Failing case: Grid wrapped in Composite, listener on the inner Grid
        Span compositeGridLog = log("composite-grid-log");
        // Diagnostic: listener registered on the Composite itself
        Span compositeSelfLog = log("composite-self-log");
        CustomGrid customGrid = new CustomGrid(compositeGridLog,
                compositeSelfLog);
        customGrid.setId("composite");

        // Control: plain Grid, listener on the Grid
        Span plainLog = log("plain-log");
        Grid<String> plainGrid = new Grid<>();
        plainGrid.setId("plain");
        plainGrid.addColumn(s -> s).setHeader("Value");
        plainGrid.setItems(new ArrayList<>());
        plainGrid.getListDataView()
                .addItemCountChangeListener(e -> append(plainLog, e));

        NativeButton add = new NativeButton("Add row", e -> {
            customGrid.getContent().getListDataView().addItem("row");
            plainGrid.getListDataView().addItem("row");
        });
        add.setId("add");

        add(new Div(new Span("Composite<Grid> listener on getContent(): "),
                compositeGridLog));
        add(new Div(new Span("Composite<Grid> listener on Composite: "),
                compositeSelfLog));
        add(new Div(new Span("Plain Grid listener: "), plainLog));
        add(add, customGrid, plainGrid);
    }

    private static Span log(String id) {
        Span span = new Span();
        span.setId(id);
        return span;
    }

    private static void append(Span log, ItemCountChangeEvent<?> e) {
        log.setText(log.getText() + "[" + e.getItemCount() + " src="
                + e.getSource().getClass().getSimpleName() + "] ");
    }

    private static final class CustomGrid extends Composite<Grid<String>> {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        CustomGrid(Span gridLog, Span selfLog) {
            Grid<String> content = getContent();
            content.addColumn(s -> s).setHeader("Value");
            content.setItems(new ArrayList<>());
            content.getListDataView()
                    .addItemCountChangeListener(e -> append(gridLog, e));
            ComponentEventListener<ItemCountChangeEvent<?>> listener = e -> append(
                    selfLog, e);
            ComponentUtil.addListener(this, ItemCountChangeEvent.class,
                    (ComponentEventListener) listener);
        }
    }
}
