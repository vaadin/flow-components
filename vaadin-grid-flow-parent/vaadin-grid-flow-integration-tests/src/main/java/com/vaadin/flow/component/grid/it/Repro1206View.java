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
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1206.
 *
 * A data provider emulating an unknown-size list: it reports 100 items, and
 * when the grid fetches the last page it grows the size by 100 and calls
 * refreshAll() from within fetch(). Reportedly the grid ignores the refresh
 * fired during fetch and never shows the additional items.
 */
@Route("repro-1206")
public class Repro1206View extends Div {

    private int currentSize = 100;

    public Repro1206View() {
        Span sizeLog = new Span("reported size: " + currentSize);
        sizeLog.setId("size-log");

        GrowingDataProvider dataProvider = new GrowingDataProvider(sizeLog);

        Grid<String> grid = new Grid<>();
        grid.addColumn(item -> item).setHeader("Item");
        grid.setDataProvider(dataProvider);
        grid.setHeight("400px");

        NativeButton refreshButton = new NativeButton(
                "refresh all (outside fetch)", e -> dataProvider.refreshAll());
        refreshButton.setId("refresh-button");

        add(grid, sizeLog, refreshButton);
    }

    private class GrowingDataProvider
            extends AbstractBackEndDataProvider<String, Void> {

        private final Span sizeLog;

        GrowingDataProvider(Span sizeLog) {
            this.sizeLog = sizeLog;
        }

        @Override
        protected Stream<String> fetchFromBackEnd(Query<String, Void> query) {
            int end = query.getOffset() + query.getLimit();
            if (end >= currentSize) {
                // more items "appear" in the backend; notify the grid from
                // within fetch, as in the original report
                currentSize += 100;
                sizeLog.setText("reported size: " + currentSize);
                refreshAll();
            }
            return IntStream
                    .range(query.getOffset(),
                            Math.min(query.getOffset() + query.getLimit(),
                                    currentSize))
                    .mapToObj(i -> "Item " + i);
        }

        @Override
        protected int sizeInBackEnd(Query<String, Void> query) {
            return currentSize;
        }
    }
}
