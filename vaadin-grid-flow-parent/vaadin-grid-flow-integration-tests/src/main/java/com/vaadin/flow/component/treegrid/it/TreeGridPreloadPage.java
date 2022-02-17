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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;

@Route("vaadin-grid/treegrid-preload/:expandedRootIndexes?([0-9,]{1,9})")
public class TreeGridPreloadPage extends VerticalLayout
        implements BeforeEnterObserver {

    private TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();

    private VaadinRequest lastRequest;
    private int requestCount = 0;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("expandedRootIndexes")
                .ifPresent(string -> {
                    List<HierarchicalTestBean> expandedRootItems = Arrays
                            .stream(string.split(",")).map(Integer::parseInt)
                            .map(expandedRootIndex -> new HierarchicalTestBean(
                                    null, 0, expandedRootIndex))
                            .collect(java.util.stream.Collectors.toList());
                    grid.expandRecursively(expandedRootItems,
                            Integer.MAX_VALUE);
                });
    }

    public TreeGridPreloadPage() {
        TextField requestCountField = new TextField(
                "Child item fetching requests");
        requestCountField.setId("request-count");
        requestCountField.setValue("0");
        requestCountField.setReadOnly(true);
        requestCountField.setWidth("300px");
        Button requestCountResetButton = new Button("Reset", event -> {
            requestCount = 0;
            requestCountField.setValue("0");
        });
        requestCountResetButton.setId("request-count-reset");
        HorizontalLayout requestCountLayout = new HorizontalLayout(
                requestCountField, requestCountResetButton);
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
        grid.setDataProvider(new LazyHierarchicalDataProvider(3, 4) {
            @Override
            protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
                    HierarchicalQuery<HierarchicalTestBean, Void> query) {
                VaadinRequest currentRequest = VaadinService
                        .getCurrentRequest();
                if (!currentRequest.equals(lastRequest)) {
                    requestCount++;
                }
                lastRequest = currentRequest;
                requestCountField.setValue(String.valueOf(requestCount));
                return super.fetchChildrenFromBackEnd(query);
            }
        });

        add(grid);
        add(requestCountLayout);
        add(receivedParentsField);
    }
}
