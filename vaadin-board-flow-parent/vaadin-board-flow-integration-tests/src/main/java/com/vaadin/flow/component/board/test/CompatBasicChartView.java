package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.Component;

/**
 *
 */
public abstract class CompatBasicChartView extends AbstractComponentTestView {

    protected abstract com.vaadin.flow.component.Component nextChartInstance();

    @Override
    protected Component[] createTestedComponents() {
        Component[] comps = { nextChartInstance(), nextChartInstance(),
                nextChartInstance(), nextChartInstance() };
        return comps;
    }

    // protected Color[] getThemeColors() {
    // Theme theme = ChartOptions.get().getTheme();
    // return (theme != null) ? theme.getColors() : new ValoLightTheme()
    // .getColors();
    // }
    //
    // protected Theme getCurrentTheme() {
    // Theme theme = ChartOptions.get().getTheme();
    // return (theme != null) ? theme : new ValoLightTheme();
    // }
}
