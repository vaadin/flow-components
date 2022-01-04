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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;

@Route("vaadin-grid/treegrid-scrolling")
public class TreeGridScrollingPage extends Div {

    public static final int DEFAULT_NODES = 20;
    public static final int DEFAULT_DEPTH = 3;
    public static final String NODES_PARAMETER = "nodes";
    public static final String DEPTH_PARAMETER = "depth";

    public TreeGridScrollingPage() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        int depth = DEFAULT_DEPTH;
        if (request.getParameter(DEPTH_PARAMETER) != null) {
            depth = Integer.parseInt(request.getParameter(DEPTH_PARAMETER));
        }
        int nodes = DEFAULT_NODES;
        if (request.getParameter(NODES_PARAMETER) != null) {
            nodes = Integer.parseInt(request.getParameter(NODES_PARAMETER));
        }

        TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();
        grid.setId("treegrid");
        grid.setSizeFull();
        grid.addHierarchyColumn(HierarchicalTestBean::toString)
                .setHeader("String").setId("string");
        grid.addColumn(HierarchicalTestBean::getDepth).setHeader("Depth")
                .setId(DEPTH_PARAMETER);
        grid.addColumn(HierarchicalTestBean::getIndex)
                .setHeader("Index on this depth").setId("index");
        grid.setDataProvider(new LazyHierarchicalDataProvider(nodes, depth));
        add(grid);
        setSizeFull();
    }
}
