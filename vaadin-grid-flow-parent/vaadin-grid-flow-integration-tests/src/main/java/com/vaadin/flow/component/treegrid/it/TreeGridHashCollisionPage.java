/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/treegrid-hash-collision")
public class TreeGridHashCollisionPage extends VerticalLayout {
    private final TreeGrid<String> treeGrid = new TreeGrid<>();

    public TreeGridHashCollisionPage() {
        TreeData<String> stringTreeData = new TreeData<>();
        // "Aa".hashCode() == "BB".hashCode()
        // Thus if TreeGrid identity provider is based on hash, collision will
        // happen and TreeGrid will fail to render correctly
        stringTreeData.addItem(null, "Aa");
        stringTreeData.addItem("Aa", "BB");
        treeGrid.setDataProvider(new TreeDataProvider<>(stringTreeData));
        treeGrid.addHierarchyColumn(s -> s);
        treeGrid.expand("Aa");
        add(treeGrid);
    }
}
