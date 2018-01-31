package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.test.CompatChartComponents;

public class BarIT extends CompatChartsUIIT{
    @Override
    protected Class<?> getUIClass() {
        return CompatChartComponents.BarChartUI.class;
    }
}
