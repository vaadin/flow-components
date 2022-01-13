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

import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("vaadin-grid/treegrid-filter-expand")
public class TreeGridFilterExpandView extends VerticalLayout {

    public TreeGridFilterExpandView() {
        setSizeFull();

        TreeGrid<String> grid = new TreeGrid<>();
        grid.addHierarchyColumn(s -> s);
        TreeDataProvider<String> dataProvider = new TreeDataProvider<>(
                generateData());
        grid.setDataProvider(dataProvider);

        Input filterField = new Input();
        filterField.setValueChangeMode(ValueChangeMode.EAGER);

        filterField.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                dataProvider.setFilter(null);
            } else {
                dataProvider.setFilter(item -> item.toLowerCase()
                        .contains(event.getValue().toLowerCase()));
            }
            grid.expandRecursively(dataProvider.getTreeData().getRootItems(),
                    99);
        });
        add(filterField, grid);
    }

    /**
     * The conditions for bug
     * https://github.com/vaadin/vaadin-grid-flow/issues/891 are quite specific,
     * and hard to figure out exactly, so this data should not be changed in
     * order to keep the test valid.
     */
    private TreeData<String> generateData() {
        TreeData<String> data = new TreeData<>();

        data.addRootItems("planning", "transportation", "personnel logistics",
                "vessel log", "configuration", "system", "user data");

        data.addItems("planning", //
                "operational plan", "transportation plan", "group schedule",
                "bulk schedule", "personnel schedule", "generate schedule log",
                "line overview", "resource plan", "shift plan");

        data.addItems("transportation", //
                "transportation summary", "visual transportation summary",
                "visual status", "visual analysis", "map", "ata/atd",
                "dfr control", "dummy transportation", "traffic log");

        data.addItems("personnel logistics", //
                "reservation", "check-in", "request & approval",
                "group schedule: request & approval", "pob plan",
                "approve purpose of visit", "approve replacement", "cargo",
                "standby", "waiting list", "upload reservation",
                "information to traveller", "reservations with deviations",
                "move to new company / department / job",
                "charter reservations", "reservation overview",
                "terminal process");
        data.addItems("terminal process", //
                "id control", "luggage drop", "security control",
                "survival suit delivery", "safety briefing",
                "terminal overview");

        data.addItems("vessel log", //
                "vessel activities", "vessel current totals");

        return data;
    }

}
