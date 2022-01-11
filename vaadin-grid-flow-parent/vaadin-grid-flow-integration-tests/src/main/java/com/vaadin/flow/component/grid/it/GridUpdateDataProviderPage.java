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

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-page")
public class GridUpdateDataProviderPage extends Div {

    public static class Pojo {

        private int id;
        private String value;

        public Pojo(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Pojo other = (Pojo) obj;
            return this.id == other.id;
        }

    }

    /**
     * Creates a view with a grid.
     */
    public GridUpdateDataProviderPage() {
        createBasicGrid();
        createBeanGrid();
    }

    private void createBasicGrid() {
        Grid<String> grid = new Grid<>();
        grid.setId("basic-grid");

        DataProvider<String, ?> orig = DataProvider
                .fromCallbacks(query -> IntStream
                        .range(query.getOffset(),
                                query.getOffset() + query.getLimit())
                        .mapToObj(Integer::toString), query -> 10000);

        DataProvider<String, ?> updated = DataProvider.ofItems("foo", "foob",
                "fooba", "foobar");

        grid.setDataProvider(orig);
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");

        NativeButton updateProvider = new NativeButton("Use another provider",
                event -> swapDataProviders(grid, orig, updated));
        updateProvider.setId("update-basic-provider");

        add(new H2("Basic grid"), grid, updateProvider);
    }

    private void createBeanGrid() {
        Grid<Pojo> grid = new Grid<>(Pojo.class);
        grid.setId("bean-grid");

        List<Pojo> orig = Arrays.asList(new Pojo(1, "foo"), new Pojo(2, "bar"));
        List<Pojo> updated = Arrays.asList(new Pojo(1, "FOOBAR"),
                new Pojo(2, "bar"));

        grid.setItems(orig);

        NativeButton updateProvider = new NativeButton("Use another list",
                event -> grid.setItems(updated));
        updateProvider.setId("update-bean-provider");

        add(new H2("Bean grid"), grid, updateProvider);
    }

    private <T> void swapDataProviders(Grid<T> grid,
            DataProvider<T, ?> provider1, DataProvider<T, ?> provider2) {
        if (grid.getDataProvider().equals(provider1)) {
            grid.setDataProvider(provider2);
        } else {
            grid.setDataProvider(provider1);
        }
    }

}
