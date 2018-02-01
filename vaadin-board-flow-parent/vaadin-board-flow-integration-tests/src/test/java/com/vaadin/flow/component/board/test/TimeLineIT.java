package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.test.CompatChartComponents;

public class TimeLineIT extends AbstractCompatChartsIT {
    @Override
    protected Class<?> getViewClass() {
        return CompatChartComponents.TimeLineView.class;
    }
}
