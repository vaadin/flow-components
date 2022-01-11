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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/select-during-data-provider-change")
public class SelectDuringDataProviderChangePage extends VerticalLayout {
    private final Grid<Item> grid = new Grid<>();
    private final Button btn = new Button("Set items");

    public SelectDuringDataProviderChangePage() {
        grid.setItems(new Item("TEST A"), new Item("TEST B"));

        grid.addColumn(Item::getName);

        btn.addClickListener(ev -> setItems());

        grid.setItemDetailsRenderer(TemplateRenderer.<Item> of("<div></div>")
                .withProperty("id", i -> 1));
        add(grid, btn);
    }

    private void setItems() {
        grid.setItems(new Item("TEST1"), new Item("TEST2"));
        try {
            // Simulate heavy work
            Thread.sleep(3000);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e); // NOSONAR
        }

    }

    public class Item {
        private final String name;

        public Item(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
