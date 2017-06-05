package com.vaadin.addon.board.testbenchtests;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.board.testUI.CompatChartComponents;

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
