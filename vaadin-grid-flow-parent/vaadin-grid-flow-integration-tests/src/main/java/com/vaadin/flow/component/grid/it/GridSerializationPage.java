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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-serialization-page")
public class GridSerializationPage extends Div {

    public static class Pojo {

        private int id;
        private String value;
        private LocalDate localDate = LocalDate.of(2018, 4, 1);
        private Integer integer = 69;

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

        public LocalDate getLocalDate() {
            return localDate;
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        public Integer getInteger() {
            return integer;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
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

        @Override
        public String toString() {
            return "Person with id " + id;
        }

    }

    public GridSerializationPage() {
        Grid<Pojo> grid = new Grid<>(Pojo.class);
        grid.setId("grid");

        List<Pojo> orig = Arrays.asList(new Pojo(1, "foo"), new Pojo(2, "bar"));
        grid.addColumn(p -> p.getLocalDate()).setHeader("Date 2");
        grid.addColumn(p -> p.getInteger()).setHeader("Int 2");
        grid.addColumn(ValueProvider.identity()).setHeader("Object");

        grid.setItems(orig);
        add(grid);
    }
}
