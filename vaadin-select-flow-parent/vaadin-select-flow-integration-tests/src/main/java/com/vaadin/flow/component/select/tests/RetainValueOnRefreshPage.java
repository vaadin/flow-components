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
package com.vaadin.flow.component.select.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Route("vaadin-select/retain-value-on-refresh")
public class RetainValueOnRefreshPage extends Div {

    public RetainValueOnRefreshPage() {
        Select<String> select = new Select<>();

        List<String> items = new LinkedList<>(
                Arrays.asList("foo", "bar", "baz", "qux"));
        select.setItems(new ListDataProvider<>(items));

        Div valueDiv = new Div();
        valueDiv.setId("value-div");
        select.addValueChangeListener(e -> valueDiv.setText(e.getValue()));

        NativeButton refreshAllButton = new NativeButton("Refresh all items",
                e -> select.getListDataView().refreshAll());
        refreshAllButton.setId("refresh-all-items");

        NativeButton updateItemsButton = new NativeButton("Update items", e -> {
            items.add("quux");
            items.remove(0);
            select.getListDataView().refreshAll();
        });
        updateItemsButton.setId("update-items");

        NativeButton updateItemLabelGeneratorButton = new NativeButton(
                "Update labels",
                e -> select.setItemLabelGenerator(item -> item + " (Updated)"));
        updateItemLabelGeneratorButton.setId("update-labels");

        add(select, refreshAllButton, updateItemsButton,
                updateItemLabelGeneratorButton, valueDiv);
    }
}
