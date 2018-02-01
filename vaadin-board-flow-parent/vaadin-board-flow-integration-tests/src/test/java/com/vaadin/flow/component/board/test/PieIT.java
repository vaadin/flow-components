package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.test.CompatChartComponents;

public class PieIT extends AbstractCompatChartsIT {

    @Override
    protected Class<?> getViewClass() {
        return CompatChartComponents.PieChartView.class;
    }
}
