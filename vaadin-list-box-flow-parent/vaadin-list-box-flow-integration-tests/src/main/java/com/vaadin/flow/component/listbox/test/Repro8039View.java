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
package com.vaadin.flow.component.listbox.test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.router.Route;

@Route("repro-8039")
public class Repro8039View extends Div {

    private static final String[] ITEMS = { "Most recent first",
            "Rating: high to low", "Rating: low to high" };

    public Repro8039View() {
        // Failing case: tooltip set before items
        ListBox<String> tooltipFirst = new ListBox<>();
        tooltipFirst.setId("tooltip-first");
        tooltipFirst.setTooltipText("Tooltip for tooltip-first");
        tooltipFirst.setItems(ITEMS);

        // Control: items set before tooltip
        ListBox<String> itemsFirst = new ListBox<>();
        itemsFirst.setId("items-first");
        itemsFirst.setItems(ITEMS);
        itemsFirst.setTooltipText("Tooltip for items-first");

        // Same as failing case, for MultiSelectListBox
        MultiSelectListBox<String> multi = new MultiSelectListBox<>();
        multi.setId("multi-tooltip-first");
        multi.setTooltipText("Tooltip for multi");
        multi.setItems(ITEMS);

        NativeButton resetItems = new NativeButton("setItems on items-first",
                e -> itemsFirst.setItems(ITEMS));
        resetItems.setId("reset-items");

        NativeButton refreshAll = new NativeButton("refreshAll on items-first",
                e -> itemsFirst.getDataProvider().refreshAll());
        refreshAll.setId("refresh-all");

        NativeButton setTooltipAgain = new NativeButton(
                "setTooltipText on tooltip-first",
                e -> tooltipFirst.setTooltipText("Tooltip set again"));
        setTooltipAgain.setId("set-tooltip-again");

        add(tooltipFirst, itemsFirst, multi,
                new Div(resetItems, refreshAll, setTooltipAgain));
    }
}
