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
package com.vaadin.flow.component.grid.it.dataview;

import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

public abstract class AbstractItemCountGridPage extends VerticalLayout
        implements BeforeEnterObserver {

    private class LazyLoadingProvider
            extends AbstractBackEndDataProvider<String, Void> {

        @Override
        public Stream<String> fetchFromBackEnd(Query<String, Void> query) {
            int limit = query.getLimit();
            int offset = query.getOffset();
            Div log = new Div();
            log.setId("log-" + fetchQueryCount);
            log.setText(fetchQueryCount + ":"
                    + Range.withLength(query.getOffset(), query.getLimit())
                            .toString());
            Logger.getLogger(getClass().getName()).info(String.format(
                    "DataProvider Query : limit %s offset %s", limit, offset));
            return IntStream.range(offset, offset + limit)
                    .mapToObj(index -> "DataProvider Item " + index);
        }

        @Override
        protected int sizeInBackEnd(Query<String, Void> query) {
            Logger.getLogger(getClass().getName()).info(String
                    .format("DataProvider Query : SIZE: %d", dataProviderSize));
            return dataProviderSize;
        }
    }

    public static final String UNDEFINED_SIZE_BUTTON_ID = "undefined-size";
    public static final String DEFINED_SIZE_BUTTON_ID = "defined-size";
    public static final String DATA_PROVIDER_BUTTON_ID = "data-provider";
    public static final String ITEM_COUNT_ESTIMATE_INPUT = "item-count-estimate-input";
    public static final String ITEM_COUNT_ESTIMATE_INCREASE_INPUT = "item-count-estimate-increase-input";
    public static final String DATA_PROVIDER_SIZE_INPUT_ID = "data-provider-size-input";
    public static final String UNDEFINED_SIZE_BACKEND_SIZE_INPUT_ID = "fetchcallback";
    public static final int DEFAULT_DATA_PROVIDER_SIZE = 1000;

    private LazyLoadingProvider dataProvider;
    private VerticalLayout menuBar;
    private Div logPanel;
    protected IntegerField itemCountEstimateInput;
    protected IntegerField itemCountEstimateIncreaseInput;
    protected IntegerField fetchCallbackSizeInput;
    protected IntegerField dataProviderSizeInput;
    protected Grid<String> grid;

    private int fetchQueryCount = 0;
    private int dataProviderSize = DEFAULT_DATA_PROVIDER_SIZE;
    private int fetchCallbackSize = -1;

    public AbstractItemCountGridPage() {
        initGrid();

        logPanel = new Div();
        logPanel.setWidth("200px");
        logPanel.setHeight("400px");
        logPanel.add("Queries:");

        menuBar = new VerticalLayout();
        menuBar.setWidth(null);

        FlexLayout layout = new FlexLayout();
        layout.setSizeFull();
        layout.add(logPanel, grid, menuBar);
        layout.setFlexGrow(1, grid);
        add(layout);
        setFlexGrow(1, layout);
        setSizeFull();

        initDataProvider();
        initEstimateOptions();
        initDataCommunicatorOptions();
        initNavigationLinks();
    }

    private void initNavigationLinks() {
        menuBar.add("Open initially with");
        menuBar.add(new RouterLink("UndefinedSize",
                ItemCountUnknownGridPage.class));
        menuBar.add(new RouterLink("InitialSizeEstimate",
                ItemCountEstimateGridPage.class));
        menuBar.add(new RouterLink("SizeEstimateCallback",
                ItemCountEstimateIncreaseGridPage.class));
        menuBar.add(
                new RouterLink("DefinedSize", ItemCountCallbackGridPage.class));
    }

    private void initGrid() {
        grid = new Grid<>();
        grid.setItems(this::fakeFetch);
        grid.setSizeFull();

        grid.addColumn(ValueProvider.identity()).setHeader("Name");
    }

    private void initDataProvider() {
        menuBar.add("Defined / Undefined size");

        dataProvider = new LazyLoadingProvider();

        Button button1 = new Button("withUndefinedSize() -> undefined size",
                event -> switchToUndefinedSize());
        button1.setId(UNDEFINED_SIZE_BUTTON_ID);
        menuBar.add(button1);
        menuBar.add(new Hr());

        Button button2 = new Button(
                "setDataProvider(FetchCallback) -> undefined size",
                event -> switchToUndefinedSizeCallback());
        menuBar.add(button2);
        button2.setId("fetchcallback");
        dataProviderSizeInput = new IntegerField("Fixed size backend size");
        menuBar.add(dataProviderSizeInput, new Hr());

        fetchCallbackSizeInput = new IntegerField("Undefined-size backend size",
                event -> fetchCallbackSize = event.getValue());
        fetchCallbackSizeInput.setId(UNDEFINED_SIZE_BACKEND_SIZE_INPUT_ID);
        fetchCallbackSizeInput.setWidthFull();
        menuBar.add(fetchCallbackSizeInput, new Hr());

        Button button3 = new Button(
                "setDefinedSize(CountCallback) -> defined size",
                event -> switchToDefinedSize());
        menuBar.add(button3);
        button3.setId(DEFINED_SIZE_BUTTON_ID);

        Button button4 = new Button(
                "setDataProvider(DataProvider) -> defined size",
                event -> switchToDataProvider());
        menuBar.add(button4);
        button4.setId(DATA_PROVIDER_BUTTON_ID);

        dataProviderSizeInput.setId(DATA_PROVIDER_SIZE_INPUT_ID);
        dataProviderSizeInput.setValue(dataProviderSize);
        dataProviderSizeInput.setWidthFull();
        dataProviderSizeInput.addValueChangeListener(event -> {
            dataProviderSize = event.getValue();
            dataProvider.refreshAll();
        });
        dataProviderSizeInput.setEnabled(false);
        menuBar.add(dataProviderSizeInput, new Hr());

        Checkbox checkbox = new Checkbox("Show fetch query logs",
                event -> logPanel.setVisible(event.getSource().getValue()));
        checkbox.setValue(true);
        menuBar.add(checkbox);
    }

    private void initEstimateOptions() {
        menuBar.add("Item Count Estimate Configuration");

        itemCountEstimateInput = new IntegerField("setItemCountEstimate",
                event -> grid.getLazyDataView()
                        .setItemCountEstimate(event.getValue()));
        itemCountEstimateInput.setId(ITEM_COUNT_ESTIMATE_INPUT);
        itemCountEstimateInput.setWidthFull();

        itemCountEstimateIncreaseInput = new IntegerField(
                "setItemCountEstimateIncrease", event -> grid.getLazyDataView()
                        .setItemCountEstimateIncrease(event.getValue()));
        itemCountEstimateIncreaseInput
                .setId(ITEM_COUNT_ESTIMATE_INCREASE_INPUT);
        itemCountEstimateIncreaseInput.setWidthFull();
        menuBar.add(itemCountEstimateInput, itemCountEstimateIncreaseInput);
    }

    private void initDataCommunicatorOptions() {
        IntegerField pageSizeInput = new IntegerField("Page Size", event -> {
            grid.setPageSize(event.getValue());
        });
        pageSizeInput.setValue(grid.getPageSize());
        pageSizeInput.setWidthFull();

        menuBar.add("DataCommunicator Configuration");
        menuBar.add(pageSizeInput);
    }

    protected void switchToDataProvider() {
        grid.setItems(dataProvider);
        dataProviderSizeInput.setEnabled(true);
    }

    protected void switchToDefinedSize() {
        grid.getLazyDataView().setItemCountCallback(dataProvider::size);
        dataProviderSizeInput.setEnabled(true);
    }

    protected void switchToUndefinedSizeCallback() {
        grid.setItems(this::fakeFetch);
        dataProviderSizeInput.setEnabled(false);
    }

    protected void switchToUndefinedSize() {
        grid.getLazyDataView().setItemCountUnknown();
        dataProviderSizeInput.setEnabled(false);
    }

    private Stream<String> fakeFetch(Query<String, Void> query) {
        int limit = query.getLimit();
        int offset = query.getOffset();
        int lastItemToFetch = offset + limit;
        if (fetchCallbackSize > 0 && (lastItemToFetch) > fetchCallbackSize) {
            lastItemToFetch = fetchCallbackSize;
        }
        Div log = new Div();
        log.setId("log-" + fetchQueryCount);
        log.setText(fetchQueryCount + ":" + Range
                .withLength(query.getOffset(), query.getLimit()).toString());
        fetchQueryCount++;
        logPanel.addComponentAsFirst(log);
        Logger.getLogger(getClass().getName()).info(String
                .format("Callback Query : limit %s offset %s", limit, offset));
        return IntStream.range(offset, lastItemToFetch)
                .mapToObj(index -> "Callback Item " + index);
    }
}
