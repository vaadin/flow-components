package com.vaadin.addon.charts.examples;

import com.vaadin.ui.html.Div;

/**
 * Abstract class for all chart examples.
 */
public abstract class AbstractChartExample extends Div {
    public AbstractChartExample() {
        initDemo();
    }

    public abstract void initDemo();

}