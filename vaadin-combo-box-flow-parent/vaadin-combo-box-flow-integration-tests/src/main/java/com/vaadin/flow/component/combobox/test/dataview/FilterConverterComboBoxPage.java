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

package com.vaadin.flow.component.combobox.test.dataview;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("filter-converter-lazy-data-view-combo-box-page")
public class FilterConverterComboBoxPage extends Div {

    public static final String DEFINED_COUNT_COMBO_BOX_ID = "defined-size-combo-box-id";
    public static final String UNKNOWN_COUNT_COMBO_BOX_ID = "unknown-size-combo-box-id";

    private static final int ITEM_LIMIT = 500;

    public FilterConverterComboBoxPage() {
        ComboBox<ItemFilter> definedCountComboBox = new ComboBox<>();
        definedCountComboBox.setId(DEFINED_COUNT_COMBO_BOX_ID);
        definedCountComboBox.setLabel("Defined Count Combo Box");
        definedCountComboBox.setItemsWithFilterConverter(this::fetchItems,
                this::countItems, this::convertFilter);

        ComboBox<ItemFilter> unknownCountComboBox = new ComboBox<>();
        unknownCountComboBox.setId(UNKNOWN_COUNT_COMBO_BOX_ID);
        definedCountComboBox.setLabel("Unknown Count Combo Box");
        unknownCountComboBox.setItemsWithFilterConverter(this::fetchItems,
                this::convertFilter);

        add(definedCountComboBox, unknownCountComboBox);
    }

    private Stream<ItemFilter> fetchItems(Query<ItemFilter, ItemFilter> query) {
        return fetchItems(query.getFilter().orElse(null), query.getOffset(),
                query.getLimit());
    }

    private Stream<ItemFilter> fetchItems(ItemFilter filter, int offset,
            int limit) {
        return IntStream.range(0, ITEM_LIMIT).mapToObj(this::createItem)
                .filter(item -> item.getValue()
                        .contains(Optional.ofNullable(filter)
                                .orElse(new ItemFilter()).getValue()))
                .skip(offset).limit(limit);
    }

    private int countItems(Query<ItemFilter, ItemFilter> query) {
        return (int) IntStream.range(0, ITEM_LIMIT).mapToObj(this::createItem)
                .filter(item -> item.getValue().contains(
                        query.getFilter().orElse(new ItemFilter()).getValue()))
                .count();
    }

    private ItemFilter createItem(int index) {
        return new ItemFilter("Item " + index);
    }

    private ItemFilter convertFilter(String label) {
        return new ItemFilter(label);
    }

    private class ItemFilter {
        private String value;

        public ItemFilter() {
            value = "";
        }

        public ItemFilter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ItemFilter that = (ItemFilter) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
