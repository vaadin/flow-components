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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1037
 *
 * ComboBox with a lazy fetch callback. While the dropdown is open, the server
 * periodically calls dataProvider.refreshAll() (driven by UI polling to mimic
 * the reporter's coroutine flow). Expected: dropdown refreshes in place like
 * Grid. Actual (reported): dropdown "blinks" on every refresh.
 *
 * Query parameters: ?count=20 (item count), ?delay=0 (fetch delay ms).
 */
@Route("repro-1037")
public class Repro1037View extends Div implements BeforeEnterObserver {

    private Registration pollRegistration;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();

        var params = event.getLocation().getQueryParameters();
        int count = params.getSingleParameter("count").map(Integer::parseInt)
                .orElse(20);
        int delay = params.getSingleParameter("delay").map(Integer::parseInt)
                .orElse(0);

        List<String> items = IntStream.range(0, count)
                .mapToObj(i -> "Item " + i).collect(Collectors.toList());

        AtomicInteger fetchCount = new AtomicInteger();
        Span fetchCountSpan = new Span("0");
        fetchCountSpan.setId("fetch-count");

        ComboBox<String> comboBox = new ComboBox<>("Categories");
        comboBox.setId("combo");
        ComboBoxLazyDataView<String> dataView = comboBox.setItems(query -> {
            fetchCountSpan
                    .setText(String.valueOf(fetchCount.incrementAndGet()));
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            String filter = query.getFilter().orElse("").toLowerCase();
            Stream<String> stream = items.stream()
                    .filter(item -> item.toLowerCase().contains(filter));
            return stream.skip(query.getOffset()).limit(query.getLimit());
        });

        NativeButton start = new NativeButton("Start periodic refreshAll",
                e -> {
                    var ui = e.getSource().getUI().orElseThrow();
                    ui.setPollInterval(1000);
                    if (pollRegistration == null) {
                        pollRegistration = ui
                                .addPollListener(pe -> dataView.refreshAll());
                    }
                });
        start.setId("start");

        NativeButton stop = new NativeButton("Stop", e -> {
            e.getSource().getUI().orElseThrow().setPollInterval(-1);
            if (pollRegistration != null) {
                pollRegistration.remove();
                pollRegistration = null;
            }
        });
        stop.setId("stop");

        add(comboBox, new Div(start, stop),
                new Div(new Span("Fetches: "), fetchCountSpan));
    }
}
