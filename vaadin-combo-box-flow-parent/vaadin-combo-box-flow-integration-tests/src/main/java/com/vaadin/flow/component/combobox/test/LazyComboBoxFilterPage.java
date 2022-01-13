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
package com.vaadin.flow.component.combobox.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/lazy-combo-box-filter")
public class LazyComboBoxFilterPage extends VerticalLayout {

    private ArrayList<Item> allItems;
    private Span query = new Span();

    public LazyComboBoxFilterPage() {
        query.setId("query");
        add(query);
        add(getCombobox());
        allItems = IntStream.range(1, 100).mapToObj(Item::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Component getCombobox() {
        ComboBox<Item> cb = new ComboBox<>();
        cb.setItems(this::getItems);
        cb.setAllowCustomValue(true);
        cb.setAutoOpen(false);
        return cb;
    }

    private Stream<Item> getItems(Query<Item, String> qu) {
        Stream<Item> items = allItems.stream()
                .filter(i -> filter(i, qu.getFilter())).skip(qu.getOffset())
                .limit(qu.getLimit());
        query.setText("Filter: " + qu.getFilter().orElse("<undefined>")
                + " Count: " + items.count());
        return allItems.stream().filter(i -> filter(i, qu.getFilter()))
                .skip(qu.getOffset()).limit(qu.getLimit());
    }

    private boolean filter(Item i, Optional<String> filter) {
        return filter.map(f -> i.toString().startsWith(f)).orElse(true);
    }

    private static class Item implements Serializable {
        int i;

        Item(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return String.format("%02d", i);
        }
    }

}
