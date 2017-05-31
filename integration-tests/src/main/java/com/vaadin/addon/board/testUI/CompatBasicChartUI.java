package com.vaadin.addon.board.testUI;

import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.Theme;
import com.vaadin.addon.charts.themes.ValoLightTheme;
import com.vaadin.ui.Component;

/**
 *
 */
public abstract class CompatBasicChartUI extends AbstractTestCompUI {

  protected abstract Component nextChartInstance();

  @Override
  protected Component[] createTestedComponents() {
    Component[] comps={nextChartInstance(),nextChartInstance(),nextChartInstance()};
    return  comps;
  }

  protected Color[] getThemeColors() {
    Theme theme = ChartOptions.get().getTheme();
    return (theme != null) ? theme.getColors() : new ValoLightTheme()
        .getColors();
  }

  protected Theme getCurrentTheme() {
    Theme theme = ChartOptions.get().getTheme();
    return (theme != null) ? theme : new ValoLightTheme();
  }
}
