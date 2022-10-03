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
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-grid/detached-grid-with-preserve-on-refresh")
public class DetachedGridWithPreserveOnRefreshPage extends Div
        implements AfterNavigationObserver {

    static final String GRID = "detached-grid";
    static final String ADD = "show-grid-button";
    static final String REMOVE = "remove-grid-button";

    private final HorizontalLayout container;
    private Grid<String> grid;
    private Div gridSlot;

    public DetachedGridWithPreserveOnRefreshPage() {
        setSizeFull();
        container = new HorizontalLayout();
        container.setSizeFull();
        Button buttonShowTableOne = new Button("Show Grid");
        buttonShowTableOne.setId(ADD);
        container.add(buttonShowTableOne);
        buttonShowTableOne.addClickListener(e -> {
            gridSlot.removeAll();
            gridSlot.add(grid);
        });

        Button removeAll = new Button("Remove Grid",
                click -> gridSlot.removeAll());
        removeAll.setId(REMOVE);
        container.add(removeAll);

        gridSlot = new Div();
        gridSlot.setHeightFull();
        gridSlot.setWidth("50%");
        container.add(gridSlot);

        grid = new Grid<>();
        grid.setId(GRID);
        grid.addColumn(e -> e).setHeader("Column 1").setFlexGrow(1);
        grid.addColumn(e -> "data " + e).setHeader("Column 2").setFlexGrow(3);
        setItems();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        removeAll();
        add(container);
        setItems();
    }

    private void setItems() {
        grid.setItems(new ArrayList<>(Arrays.asList("1", "2", "3")));
    }
}
