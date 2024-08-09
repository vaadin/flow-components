/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-single-selection-update-and-deselect")
public class GridSingleSelectionUpdateAndDeselectPage extends Div {

    private Bean bean = new Bean(0, "Foo");

    private Bean getBeanClone() {
        return new Bean(bean.getId(), bean.getName());
    }

    public GridSingleSelectionUpdateAndDeselectPage() {
        var grid = new Grid<Bean>(Bean.class);

        grid.setItems(query -> {
            query.getPageSize();
            query.getLimit();
            query.getOffset();
            query.getPage();
            return Stream.of(getBeanClone());
        });

        grid.select(getBeanClone());

        var button = new Button("Update name", e -> {
            bean.setName("Bar");

            // The order of these two calls is important. Do not change.
            // See https://github.com/vaadin/flow-components/issues/3229
            grid.getLazyDataView().refreshAll();
            grid.select(null);
        });
        button.setId("update-name");

        add(grid, button);
    }

    public static class Bean {
        private final int id;
        private String name;

        public Bean(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Bean) {
                Bean other = (Bean) obj;
                return id == other.id;
            }
            return false;
        }
    }
}
