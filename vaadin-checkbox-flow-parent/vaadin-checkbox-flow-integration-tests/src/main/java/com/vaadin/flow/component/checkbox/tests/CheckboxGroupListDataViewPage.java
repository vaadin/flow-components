/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Route("vaadin-checkbox-group-list-data-view")
public class CheckboxGroupListDataViewPage extends Div {

    static final String CHECKBOX_GROUP = "checkbox-group-data-view";
    static final String ITEMS_SIZE = "size-span-data-view";
    static final String ITEM_PRESENT = "item-present-span-data-view";
    static final String ALL_ITEMS = "all-items-span-data-view";
    static final String ITEM_ON_INDEX = "item-on-index-data-view";
    static final String CURRENT_ITEM = "current-item-span-list-data-view";
    static final String HAS_NEXT_ITEM = "has-next-item-span-list-data-view";
    static final String HAS_PREVIOUS_ITEM = "has-prev-item-span-list-data-view";
    static final String NEXT_ITEM = "next-item-button-list-data-view";
    static final String PREVIOUS_ITEM = "prev-item-button-list-data-view";
    static final String FILTER_BUTTON = "filter-button-list-data-view";
    static final String SORT_BUTTON = "sort-button-list-data-view";

    public CheckboxGroupListDataViewPage() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        CheckboxGroupListDataView<String> dataView =
                checkboxGroup.setDataProvider("foo", "bar", "baz");

        Span sizeSpan = new Span(String.valueOf(dataView.getDataSize()));
        Span containsItemSpan = new Span(String.valueOf(dataView.isItemPresent("foo")));
        Span allItemsSpan = new Span(dataView.getAllItems().collect(Collectors.joining(",")));
        Span itemOnIndexSpan = new Span(dataView.getItemOnIndex(0));

        AtomicReference<String> currentItem = new AtomicReference<>("bar");

        Span currentItemSpan = new Span(currentItem.get());
        Span hasNextItemSpan = new Span(String.valueOf(dataView.hasNextItem("foo")));
        Span hasPrevItemSpan = new Span(String.valueOf(dataView.hasPreviousItem("bar")));

        Button nextItemButton = new Button("Next Item", event -> {
            String nextItem = dataView.getNextItem(currentItem.get());
            currentItem.set(nextItem);
            currentItemSpan.setText(currentItem.get());
        });
        Button prevItemButton = new Button("Previous Item", event -> {
            String prevItem = dataView.getPreviousItem(currentItem.get());
            currentItem.set(prevItem);
            currentItemSpan.setText(currentItem.get());
        });
        Button filterButton = new Button("Filter Items",
                event -> dataView.withFilter("bar"::equals));
        Button sortButton = new Button("Sort Items",
                event -> dataView.withSortComparator(String::compareTo));

        checkboxGroup.setId(CHECKBOX_GROUP);
        sizeSpan.setId(ITEMS_SIZE);
        containsItemSpan.setId(ITEM_PRESENT);
        allItemsSpan.setId(ALL_ITEMS);
        itemOnIndexSpan.setId(ITEM_ON_INDEX);
        currentItemSpan.setId(CURRENT_ITEM);
        hasNextItemSpan.setId(HAS_NEXT_ITEM);
        hasPrevItemSpan.setId(HAS_PREVIOUS_ITEM);
        nextItemButton.setId(NEXT_ITEM);
        prevItemButton.setId(PREVIOUS_ITEM);
        filterButton.setId(FILTER_BUTTON);
        sortButton.setId(SORT_BUTTON);

        add(checkboxGroup, sizeSpan, containsItemSpan, allItemsSpan, itemOnIndexSpan,
                currentItemSpan, hasNextItemSpan, hasPrevItemSpan, filterButton,
                sortButton, nextItemButton, prevItemButton);
    }
}
