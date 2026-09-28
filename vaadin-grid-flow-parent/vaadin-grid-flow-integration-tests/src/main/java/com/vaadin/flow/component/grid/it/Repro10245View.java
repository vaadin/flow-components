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

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/10245
 * <p>
 * Open /repro-10245?focus=10 directly: scrollToItem is called in the same
 * round trip that first renders the grid.
 */
@Route("repro-10245")
public class Repro10245View extends Div implements BeforeEnterObserver {

    private final Grid<String> grid = new Grid<>();

    public Repro10245View() {
        grid.setId("grid");
        grid.setHeight("600px");
        grid.addColumn(s -> s).setHeader("Item");
        grid.setItems(
                IntStream.range(0, 200).mapToObj(i -> "Item " + i).toList());

        // Control: same call after the grid has rendered
        NativeButton scroll10 = new NativeButton("scrollToItem(Item 10)",
                e -> grid.scrollToItem("Item 10"));
        scroll10.setId("scroll-10");
        NativeButton scrollTop = new NativeButton("scrollToStart",
                e -> grid.scrollToStart());
        scrollTop.setId("scroll-start");

        add(scroll10, scrollTop, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getLocation().getQueryParameters().getSingleParameter("focus")
                .map(Integer::valueOf)
                .ifPresent(index -> grid.scrollToItem("Item " + index));
    }
}
