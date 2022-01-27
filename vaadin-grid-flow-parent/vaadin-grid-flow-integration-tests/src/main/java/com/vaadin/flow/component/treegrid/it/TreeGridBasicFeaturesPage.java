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
package com.vaadin.flow.component.treegrid.it;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridMultiSelectionModel.SelectAllCheckboxVisibility;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.Range;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/" + TreeGridBasicFeaturesPage.VIEW)
public class TreeGridBasicFeaturesPage extends Div {

    public static final String VIEW = "treegrid-basic-features";

    private TreeGrid<HierarchicalTestBean> grid;
    private TreeDataProvider<HierarchicalTestBean> inMemoryDataProvider;
    private LazyHierarchicalDataProvider lazyDataProvider;
    private HierarchicalDataProvider<HierarchicalTestBean, ?> loggingDataProvider;
    private TextArea log;

    public TreeGridBasicFeaturesPage() {
        initializeDataProviders();
        grid = new TreeGrid<>(HierarchicalTestBean.class);
        grid.setWidth("100%");
        grid.setSelectionMode(SelectionMode.SINGLE);
        grid.setColumns("id", HierarchicalTestBean::toString,
                Arrays.asList("id", "depth", "index"));
        grid.setDataProvider(new LazyHierarchicalDataProvider(3, 2));

        grid.setId("testComponent");

        grid.getColumnByKey("depth").setHeader("Depth");
        grid.getColumnByKey("index").setHeader("Index on this depth");

        log = new TextArea();
        log.setId("log");
        log.setHeight("100px");
        log.setWidth("100%");
        add(grid, new VerticalLayout(
                setIdByText(new NativeButton("Clear log", e -> log.clear())),
                log));

        createActions();

    }

    protected void createActions() {
        createDataProviderSelect();
        createHierarchyColumnSelect();
        createExpandMenu();
        createCollapseMenu();
        createListenerMenu();
        createSelectionModeMenu();
        createDisableEnableMenu();
    }

    private void initializeDataProviders() {
        TreeData<HierarchicalTestBean> data = new TreeData<>();

        List<Integer> ints = Arrays.asList(0, 1, 2);

        ints.stream().forEach(index -> {
            HierarchicalTestBean bean = new HierarchicalTestBean(null, 0,
                    index);
            data.addItem(null, bean);
            ints.stream().forEach(childIndex -> {
                HierarchicalTestBean childBean = new HierarchicalTestBean(
                        bean.getId(), 1, childIndex);
                data.addItem(bean, childBean);
                ints.stream()
                        .forEach(grandChildIndex -> data.addItem(childBean,
                                new HierarchicalTestBean(childBean.getId(), 2,
                                        grandChildIndex)));
            });
        });

        inMemoryDataProvider = new CustomTreeDataProvider(data);
        lazyDataProvider = new LazyHierarchicalDataProvider(3, 2);
        loggingDataProvider = new CustomTreeDataProvider(data) {

            @Override
            public Stream<HierarchicalTestBean> fetchChildren(
                    HierarchicalQuery<HierarchicalTestBean, SerializablePredicate<HierarchicalTestBean>> query) {
                Optional<HierarchicalTestBean> parentOptional = query
                        .getParentOptional();
                if (parentOptional.isPresent()) {
                    log("Children request: " + parentOptional.get() + " ; "
                            + Range.withLength(query.getOffset(),
                                    query.getLimit()));
                } else {
                    log("Root node request: " + Range
                            .withLength(query.getOffset(), query.getLimit()));
                }
                return super.fetchChildren(query);
            }
        };

    }

    private void log(String txt) {
        log.setValue(txt + "\n" + log.getValue());
    }

    @SuppressWarnings("unchecked")
    private void createDataProviderSelect() {
        @SuppressWarnings("rawtypes")
        LinkedHashMap<String, DataProvider> options = new LinkedHashMap<>();
        options.put("LazyHierarchicalDataProvider", lazyDataProvider);
        options.put("TreeDataProvider", inMemoryDataProvider);
        options.put("LoggingDataProvider", loggingDataProvider);

        options.entrySet().forEach(entry -> {
            addAction(entry.getKey(),
                    () -> grid.setDataProvider(entry.getValue()));
        });
    }

    private void createHierarchyColumnSelect() {
        LinkedHashMap<String, ValueProvider<HierarchicalTestBean, ?>> options = new LinkedHashMap<>();
        options.put("id", item -> item.toString());
        options.put("depth", HierarchicalTestBean::getDepth);
        options.put("index", HierarchicalTestBean::getIndex);

        options.entrySet().forEach(entry -> {
            addAction("Set hierarchy column - " + entry.getKey(), () -> {
                grid.setHierarchyColumn(entry.getKey(), entry.getValue());
                // reset headers
                grid.getColumnByKey("depth").setHeader("Depth");
                grid.getColumnByKey("index").setHeader("Index on this depth");
            });
        });
    }

    private void createExpandMenu() {
        addAction("Expand 0 | 0",
                () -> grid.expand(new HierarchicalTestBean(null, 0, 0)));

        addAction("Expand 1 | 1",
                () -> grid.expand(new HierarchicalTestBean("/0/0", 1, 1)));

        addAction("Expand 2 | 1",
                () -> grid.expand(new HierarchicalTestBean("/0/0/1/1", 2, 1)));

        addAction("Expand 0 | 0 recursively",
                () -> grid.expandRecursively(
                        Arrays.asList(new HierarchicalTestBean(null, 0, 0)),
                        1));
    }

    private void createCollapseMenu() {
        addAction("Collapse 0 | 0",
                () -> grid.collapse(new HierarchicalTestBean(null, 0, 0)));
        addAction("Collapse 1 | 1",
                () -> grid.collapse(new HierarchicalTestBean("/0/0", 1, 1)));
        addAction("Collapse 2 | 1", () -> grid
                .collapse(new HierarchicalTestBean("/0/0/1/1", 2, 1)));
        addAction("Collapse 0 | 0 recursively",
                () -> grid.collapseRecursively(
                        Arrays.asList(new HierarchicalTestBean(null, 0, 0)),
                        2));
    }

    private void createListenerMenu() {
        addAction("Collapse listener",
                () -> grid.addCollapseListener(
                        event -> log("Item(s) collapsed (from client: "
                                + event.isFromClient() + "): "
                                + event.getItems().stream().findFirst()
                                        .map(HierarchicalTestBean::toString)
                                        .orElse("null"))));

        addAction("Expand listener", () -> grid.addExpandListener(event -> log(
                "Item(s) expanded (from client: " + event.isFromClient() + "): "
                        + event.getItems().stream().findFirst()
                                .map(HierarchicalTestBean::toString)
                                .orElse("null"))));
    }

    @SuppressWarnings("rawtypes")
    private void createSelectionModeMenu() {
        LinkedHashMap<String, SelectionMode> options = new LinkedHashMap<>();
        options.put("none", SelectionMode.NONE);
        options.put("single", SelectionMode.SINGLE);
        options.put("multi", SelectionMode.MULTI);

        options.entrySet().forEach(entry -> {
            addAction("Selection mode - " + entry.getKey(), () -> {
                grid.setSelectionMode(entry.getValue());
                if (entry.getValue() == SelectionMode.MULTI) {
                    GridSelectionModel model = grid.getSelectionModel();
                    if (model instanceof GridMultiSelectionModel) {
                        ((GridMultiSelectionModel) model)
                                .setSelectAllCheckboxVisibility(
                                        SelectAllCheckboxVisibility.VISIBLE);
                    }
                }
            });
        });
    }

    private void createDisableEnableMenu() {
        addAction("Toggle Enabled/Disabled",
                () -> grid.setEnabled(!grid.isEnabled()));
    }

    private void addAction(String title, Runnable action) {
        NativeButton b = new NativeButton(title, event -> action.run());
        setIdByText(b);
        add(b);
    }

    private NativeButton setIdByText(NativeButton button) {
        button.setId(button.getText().replace(" ", ""));
        return button;
    }

    public class CustomTreeDataProvider
            extends TreeDataProvider<HierarchicalTestBean> {
        public CustomTreeDataProvider(TreeData<HierarchicalTestBean> treeData) {
            super(treeData);
        }

        @Override
        public Object getId(HierarchicalTestBean item) {
            Objects.requireNonNull(item,
                    "Cannot provide an id for a null item.");
            return item.getId();
        }
    }
}
