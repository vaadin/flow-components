/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
