package com.vaadin.addon.board.testbenchtests;

import com.vaadin.addon.board.testUI.CompatChartComponents;

public class PieIT extends CompatChartsUIIT{
    @Override
    protected Class<?> getUIClass() {
        return CompatChartComponents.PieChartUI.class;
    }
}
