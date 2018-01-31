package com.vaadin.flow.component.board.test;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.board.test.CompatChartComponents;

public class GaugeIT extends CompatChartsUIIT{
    @Override
    protected Class<?> getUIClass() {
        return CompatChartComponents.GaugeUI.class;
    }

    @Ignore
    @Test
    public void testScreenshot() throws Exception{
        throw new Exception("Gauge chart is broken");
    }
}
