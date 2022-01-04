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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@Route("vaadin-grid/" + TreeGridOrderColumnsPage.VIEW)
public class TreeGridOrderColumnsPage extends Div {

    public static final String VIEW = "treegrid-order-columns";
    public static final String COL1_NAME = "Col1";
    public static final String COL2_NAME = "Col2";
    public static final String COL3_NAME = "Col3";
    public static final String HEADER2_PREFIX = "Header 2 ";
    public static final String HEADER3_PREFIX = "Header 3 ";

    private TreeGrid<HierarchicalTestBean> treeGrid;

    public TreeGridOrderColumnsPage() {
        initializeDataProviders();
        treeGrid = new TreeGrid<>(HierarchicalTestBean.class);
        treeGrid.setColumnReorderingAllowed(true);
        treeGrid.setWidth("100%");
        treeGrid.setSelectionMode(SelectionMode.MULTI);
        treeGrid.setColumns("id", HierarchicalTestBean::toString,
                Arrays.asList("id", "depth", "index"));
        treeGrid.setDataProvider(new LazyHierarchicalDataProvider(3, 2));

        treeGrid.setId("testComponent");

        Grid.Column<HierarchicalTestBean> column1 = treeGrid
                .getColumnByKey("id");
        column1.setHeader(COL1_NAME);
        Grid.Column<HierarchicalTestBean> column2 = treeGrid
                .getColumnByKey("depth");
        column2.setHeader(COL2_NAME);
        Grid.Column<HierarchicalTestBean> column3 = treeGrid
                .getColumnByKey("index");
        column3.setHeader(COL3_NAME);

        HeaderRow row1 = treeGrid.appendHeaderRow();
        row1.getCell(column1).setText(HEADER2_PREFIX + COL1_NAME);
        row1.getCell(column2).setText(HEADER2_PREFIX + COL2_NAME);
        row1.getCell(column3).setText(HEADER2_PREFIX + COL3_NAME);
        HeaderRow row2 = treeGrid.appendHeaderRow();
        row2.getCell(column1).setText(HEADER3_PREFIX + COL1_NAME);
        row2.getCell(column2).setText(HEADER3_PREFIX + COL2_NAME);
        row2.getCell(column3).setText(HEADER3_PREFIX + COL3_NAME);
        add(treeGrid);

        Button orderCol123Button = new Button("Col 1 2 3 ",
                e -> treeGrid.setColumnOrder(column1, column2, column3));
        orderCol123Button.setId("button-123");
        Button orderCol321Button = new Button("Col 3 2 1 ",
                e -> treeGrid.setColumnOrder(column3, column2, column1));
        orderCol321Button.setId("button-321");

        add(orderCol123Button, orderCol321Button);
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
    }

}
