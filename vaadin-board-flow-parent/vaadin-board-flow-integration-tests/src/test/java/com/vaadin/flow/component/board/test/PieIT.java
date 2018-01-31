package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.test.CompatChartComponents;

public class PieIT extends CompatChartsUIIT{
    @Override
    protected Class<?> getUIClass() {
        return CompatChartComponents.PieChartUI.class;
    }
}
