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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/select-all")
public class MultiSelectComboBoxSelectAllPage extends Div {
    public MultiSelectComboBoxSelectAllPage() {
        List<String> items = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> "Item " + i).toList();

        // More items than the page size, so that filtering happens on the
        // server and not all items are loaded to the client
        MultiSelectComboBox<String> serverSideComboBox = new MultiSelectComboBox<>(
                "Server-side filtering");
        serverSideComboBox.setItems(items);
        setupComboBox(serverSideComboBox, "server-side");

        // Items that fit into one page are filtered on the client
        MultiSelectComboBox<String> clientSideComboBox = new MultiSelectComboBox<>(
                "Client-side filtering");
        clientSideComboBox.setItems(items.subList(0, 10));
        setupComboBox(clientSideComboBox, "client-side");

        // Switches the first combo box to client-side filtering
        NativeButton setFewItems = new NativeButton("Set few items",
                e -> serverSideComboBox.setItems(items.subList(0, 10)));
        setFewItems.setId("set-few-items");

        add(setFewItems);
    }

    private void setupComboBox(MultiSelectComboBox<String> comboBox,
            String id) {
        comboBox.setId(id);
        comboBox.setSelectAllButtonVisible(true);
        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("500px");

        Span eventCount = new Span("0");
        eventCount.setId(id + "-event-count");
        Span eventValueSize = new Span();
        eventValueSize.setId(id + "-event-value-size");
        Span eventOrigin = new Span();
        eventOrigin.setId(id + "-event-origin");
        comboBox.addValueChangeListener(e -> {
            eventCount.setText(
                    String.valueOf(Integer.parseInt(eventCount.getText()) + 1));
            eventValueSize.setText(String.valueOf(e.getValue().size()));
            eventOrigin.setText(e.isFromClient() ? "client" : "server");
        });

        add(comboBox);
        add(new Div(new Span("Event count: "), eventCount));
        add(new Div(new Span("Event value size: "), eventValueSize));
        add(new Div(new Span("Event origin: "), eventOrigin));
    }
}
