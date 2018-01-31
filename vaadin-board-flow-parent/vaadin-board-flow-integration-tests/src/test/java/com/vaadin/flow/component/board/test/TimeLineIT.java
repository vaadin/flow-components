package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.board.test.CompatChartComponents;

public class TimeLineIT extends CompatChartsUIIT{
    @Override
    protected Class<?> getUIClass() {
        return CompatChartComponents.TimeLineUI.class;
    }
}
