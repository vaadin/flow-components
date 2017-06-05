package com.vaadin.addon.board.testbenchtests;

import com.vaadin.addon.board.testUI.CompatChartComponents;

public class TimeLineIT extends CompatChartsUIIT{
    @Override
    protected Class<?> getUIClass() {
        return CompatChartComponents.TimeLineUI.class;
    }
}
