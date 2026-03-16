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
package com.vaadin.flow.component.radiobutton.tests;

import java.util.ArrayList;
import java.util.stream.IntStream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/selection-preservation")
public class SelectionPreservationPage extends Div {

    public SelectionPreservationPage() {
        var items = new ArrayList<>(IntStream.range(0, 10).boxed().toList());
        var dataProvider = new ListDataProvider<>(items);

        var group = new RadioButtonGroup<Integer>();
        group.setItems(dataProvider);
        group.setItemLabelGenerator(String::valueOf);

        var modePreserveAll = new NativeButton("Preserve All",
                e -> group.setSelectionPreservationMode(
                        SelectionPreservationMode.PRESERVE_ALL));
        modePreserveAll.setId("mode-preserve-all");

        var modePreserveExisting = new NativeButton("Preserve Existing",
                e -> group.setSelectionPreservationMode(
                        SelectionPreservationMode.PRESERVE_EXISTING));
        modePreserveExisting.setId("mode-preserve-existing");

        var modeDiscard = new NativeButton("Discard",
                e -> group.setSelectionPreservationMode(
                        SelectionPreservationMode.DISCARD));
        modeDiscard.setId("mode-discard");

        var refreshAll = new NativeButton("Refresh all",
                e -> dataProvider.refreshAll());
        refreshAll.setId("refresh-all");

        var removeItem5 = new NativeButton("Remove item 5",
                e -> items.remove(Integer.valueOf(5)));
        removeItem5.setId("remove-item-5");

        var serverValue = new Span();
        serverValue.setId("server-value");

        var showServerValue = new NativeButton("Show server value", e -> {
            var value = group.getValue();
            serverValue.setText(value != null ? String.valueOf(value) : "");
        });
        showServerValue.setId("show-server-value");

        var modeButtons = new Div(modePreserveAll, modePreserveExisting,
                modeDiscard);
        var actionButtons = new Div(refreshAll, removeItem5, showServerValue);

        add(group, modeButtons, actionButtons, serverValue);
    }
}
