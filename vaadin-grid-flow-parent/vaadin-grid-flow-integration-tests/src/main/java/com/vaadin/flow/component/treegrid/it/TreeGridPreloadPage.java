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
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;

@Route("vaadin-grid/treegrid-preload")
public class TreeGridPreloadPage extends VerticalLayout
        implements HasUrlParameter<String> {

    private TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();
    private TextField requestCountField = new TextField(
            "Child item fetching requests");
    private TextField fetchCountField = new TextField("Data provider fetches");

    private VaadinRequest lastRequest;
    private int requestCount = 0;
    private int fetchCount = 0;

    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String parameter) {

        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        // query parameter: pageSize
        List<String> pageSize = queryParameters.getParameters().get("pageSize");
        if (pageSize != null) {
            grid.setPageSize(Integer.parseInt(pageSize.get(0)));
        }

        // query parameter: nodesPerLevel
        List<String> nodesPerLevel = queryParameters.getParameters()
                .get("nodesPerLevel");

        // query parameter: depth
        List<String> depth = queryParameters.getParameters().get("depth");

        int dpNodesPerLevel = nodesPerLevel == null ? 3
                : Integer.parseInt(nodesPerLevel.get(0));
        int dpDepth = depth == null ? 4 : Integer.parseInt(depth.get(0));
        setDataProvider(dpNodesPerLevel, dpDepth);

        // query parameter: expandedRootIndexes
        List<String> expandedRootIndexes = queryParameters.getParameters()
                .get("expandedRootIndexes");
        if (expandedRootIndexes != null) {
            List<HierarchicalTestBean> expandedRootItems = Arrays
                    .stream(expandedRootIndexes.get(0).split(","))
                    .map(Integer::parseInt)
                    .map(expandedRootIndex -> new HierarchicalTestBean(null, 0,
                            expandedRootIndex))
                    .collect(java.util.stream.Collectors.toList());
            grid.expandRecursively(expandedRootItems, Integer.MAX_VALUE);
        }

        // query parameter: sortDirection
        List<String> sortDirection = queryParameters.getParameters()
                .get("sortDirection");
        if (sortDirection != null) {
            SortDirection direction = SortDirection
                    .valueOf(sortDirection.get(0).toUpperCase());
            GridSortOrderBuilder<HierarchicalTestBean> sorting = new GridSortOrderBuilder<HierarchicalTestBean>();
            Column<HierarchicalTestBean> column = grid.getColumns().get(0);
            if (direction == SortDirection.ASCENDING) {
                grid.sort(sorting.thenAsc(column).build());
            } else {
                grid.sort(sorting.thenDesc(column).build());
            }
        }
    }

    private void setDataProvider(int nodesPerLevel, int depth) {
        grid.setDataProvider(
                new LazyHierarchicalDataProvider(nodesPerLevel, depth) {
                    @Override
                    protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
                            HierarchicalQuery<HierarchicalTestBean, Void> query) {
                        VaadinRequest currentRequest = VaadinService
                                .getCurrentRequest();
                        if (!currentRequest.equals(lastRequest)) {
                            requestCount++;
                        }
                        lastRequest = currentRequest;
                        requestCountField
                                .setValue(String.valueOf(requestCount));

                        fetchCount++;
                        fetchCountField.setValue(String.valueOf(fetchCount));

                        return super.fetchChildrenFromBackEnd(query);
                    }

                    @Override
                    public Object getId(HierarchicalTestBean item) {
                        return item != null ? item.getId() : "null";
                    }
                });
    }

    public TreeGridPreloadPage() {
        requestCountField.setId("request-count");
        requestCountField.setValue("0");
        requestCountField.setReadOnly(true);
        requestCountField.setWidth("300px");

        fetchCountField.setId("fetch-count");
        fetchCountField.setValue("0");
        fetchCountField.setReadOnly(true);

        Button requestCountResetButton = new Button("Reset", event -> {
            requestCount = 0;
            requestCountField.setValue("0");
            fetchCount = 0;
            fetchCountField.setValue("0");
        });
        requestCountResetButton.setId("request-count-reset");
        HorizontalLayout requestCountLayout = new HorizontalLayout(
                requestCountField, fetchCountField, requestCountResetButton);
        requestCountLayout.setAlignItems(Alignment.END);

        TextArea receivedParentsField = new TextArea(
                "Parents with received children");
        receivedParentsField.setReadOnly(true);
        receivedParentsField.setId("received-parents");
        receivedParentsField.setHeight("200px");
        receivedParentsField.setWidth("300px");

        grid.getElement().executeJs(
                "const confirmParent = this.$connector.confirmParent;"
                        + "this.$connector.confirmParent = function(id, parentKey, levelSize) {"
                        + "  confirmParent.call(this.$connector, id, parentKey, levelSize);"
                        + "  window.receivedParents = window.receivedParents || new Set();"
                        + "  window.receivedParents.add(parentKey);"
                        + "  document.getElementById('received-parents').value = [...window.receivedParents].join('\\n');"
                        + "  document.getElementById('received-parents').helperText = 'Items: (' + window.receivedParents.size + ')';"
                        + "}");

        grid.addHierarchyColumn(HierarchicalTestBean::getId).setHeader("Id");
        grid.addColumn(HierarchicalTestBean::getDepth).setHeader("Depth");
        grid.addColumn(HierarchicalTestBean::getIndex)
                .setHeader("Index on level");
        grid.addColumn(LitRenderer.of("${index}")).setHeader("Index");
        grid.setUniqueKeyDataGenerator("key",
                item -> item != null ? item.getId() : "null");

        add(grid);
        add(requestCountLayout);
        add(receivedParentsField);
    }
}
